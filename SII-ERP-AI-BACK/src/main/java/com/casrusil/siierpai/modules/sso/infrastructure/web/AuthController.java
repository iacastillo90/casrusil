package com.casrusil.siierpai.modules.sso.infrastructure.web;

import com.casrusil.siierpai.modules.sso.application.dto.AuthResult;
import com.casrusil.siierpai.modules.sso.application.service.AuthService;
import com.casrusil.siierpai.modules.sso.domain.model.Company;
import com.casrusil.siierpai.modules.sso.domain.model.User;
import com.casrusil.siierpai.modules.sso.domain.port.out.CompanyRepository;
import com.casrusil.siierpai.modules.sso.domain.port.out.UserRepository;
import com.casrusil.siierpai.modules.sso.infrastructure.security.JwtTokenProvider;
import com.casrusil.siierpai.modules.sso.infrastructure.security.LoginRateLimiter;
import com.casrusil.siierpai.modules.sso.infrastructure.security.SecurityFilter;
import com.casrusil.siierpai.modules.sso.infrastructure.security.TokenBlacklistService;
import com.casrusil.siierpai.modules.sso.infrastructure.web.dto.AuthResponse;
import com.casrusil.siierpai.modules.sso.infrastructure.web.dto.MeResponse;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import com.casrusil.siierpai.shared.domain.valueobject.UserId;
import com.casrusil.siierpai.shared.infrastructure.context.CompanyContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.List;

/**
 * Controlador REST para autenticación pública.
 *
 * <p>
 * Maneja registro, login y logout. El token JWT se entrega como cookie
 * {@code HttpOnly} (no accesible por JavaScript, mitiga XSS) además de en el
 * cuerpo de la respuesta para clientes de API. Aplica rate limiting anti
 * fuerza-bruta por IP.
 *
 * <h2>Endpoints:</h2>
 * <ul>
 * <li>{@code POST /api/v1/auth/register}: Registrar nueva empresa.</li>
 * <li>{@code POST /api/v1/auth/login}: Iniciar sesión.</li>
 * <li>{@code POST /api/v1/auth/logout}: Cerrar sesión (revoca el token).</li>
 * </ul>
 *
 * @see AuthService
 * @since 1.0
 */
@RestController
@RequestMapping({ "/api/v1/auth", "/auth" })
public class AuthController {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;
    private final LoginRateLimiter loginRateLimiter;
    private final TokenBlacklistService tokenBlacklistService;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;

    /** true en producción para exigir HTTPS en la cookie (Secure). */
    @Value("${security.cookie.secure:false}")
    private boolean cookieSecure;

    @Value("${security.cookie.same-site:Strict}")
    private String cookieSameSite;

    public AuthController(AuthService authService,
            LoginRateLimiter loginRateLimiter,
            TokenBlacklistService tokenBlacklistService,
            JwtTokenProvider jwtTokenProvider,
            UserRepository userRepository,
            CompanyRepository companyRepository) {
        this.authService = authService;
        this.loginRateLimiter = loginRateLimiter;
        this.tokenBlacklistService = tokenBlacklistService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
    }

    /**
     * Devuelve el usuario autenticado desde el JWT.
     *
     * <p>
     * Requiere una sesión válida (cookie {@code access_token} o header
     * {@code Authorization: Bearer ...}). Sin sesión responde {@code 401}.
     * </p>
     *
     * @return identidad y contexto de empresa del usuario
     */
    @GetMapping("/me")
    public ResponseEntity<MeResponse> me() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserId userId)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = userRepository.findById(userId).orElse(null);
        if (user == null || !user.isActive()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        CompanyId tokenCompanyId = CompanyContext.getCompanyId();
        if (tokenCompanyId != null && !tokenCompanyId.getValue().equals(user.getCompanyId().getValue())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Company company = companyRepository.findById(user.getCompanyId()).orElse(null);
        if (company == null || !company.isActive()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<MeResponse.CompanySummary> companies = List.of(
                new MeResponse.CompanySummary(
                        company.getId().getValue(),
                        company.getRazonSocial(),
                        company.getRut()));

        return ResponseEntity.ok(new MeResponse(
                user.getId().getValue(),
                user.getEmail(),
                user.getFullName(),
                user.getRole().name(),
                user.getCompanyId().getValue(),
                companies));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest) {
        String clientId = clientIdentifier(httpRequest);
        enforceRateLimit(clientId);
        try {
            AuthResult result = authService.registerCompany(
                    request.rut(), request.razonSocial(), request.adminEmail(), request.adminPassword());
            loginRateLimiter.reset(clientId);
            return okWithCookie(result);
        } catch (RuntimeException e) {
            loginRateLimiter.recordFailedAttempt(clientId);
            throw e;
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        String clientId = clientIdentifier(httpRequest);
        enforceRateLimit(clientId);
        try {
            AuthResult result = authService.login(request.email(), request.password());
            loginRateLimiter.reset(clientId);
            return okWithCookie(result);
        } catch (RuntimeException e) {
            loginRateLimiter.recordFailedAttempt(clientId);
            throw e;
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest httpRequest) {
        String token = extractToken(httpRequest);
        if (token != null && jwtTokenProvider.validateToken(token)) {
            tokenBlacklistService.blacklist(token, jwtTokenProvider.getExpiration(token));
        }
        // Cookie vacía y expirada para borrarla en el navegador.
        ResponseCookie cleared = baseCookie("", Duration.ZERO).build();
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, cleared.toString())
                .build();
    }

    // ----------------------------------------------------------------------

    private void enforceRateLimit(String clientId) {
        if (loginRateLimiter.isBlocked(clientId)) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "Demasiados intentos. Intenta nuevamente más tarde.");
        }
    }

    private ResponseEntity<AuthResponse> okWithCookie(AuthResult result) {
        ResponseCookie cookie = baseCookie(result.token(),
                Duration.ofMillis(jwtTokenProvider.getExpirationMillis())).build();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(mapToResponse(result));
    }

    private ResponseCookie.ResponseCookieBuilder baseCookie(String value, Duration maxAge) {
        return ResponseCookie.from(SecurityFilter.ACCESS_TOKEN_COOKIE, value)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite(cookieSameSite)
                .path("/")
                .maxAge(maxAge);
    }

    private String extractToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        if (request.getCookies() != null) {
            for (jakarta.servlet.http.Cookie c : request.getCookies()) {
                if (SecurityFilter.ACCESS_TOKEN_COOKIE.equals(c.getName())) {
                    return c.getValue();
                }
            }
        }
        return null;
    }

    /** Identificador para rate limiting: IP del cliente (respeta X-Forwarded-For). */
    private String clientIdentifier(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private AuthResponse mapToResponse(AuthResult result) {
        return new AuthResponse(
                result.token(),
                result.user().getId().getValue(),
                result.user().getEmail(),
                result.company().getId().getValue(),
                result.company().getRazonSocial(),
                result.user().getRole().name());
    }

    public record RegisterRequest(
            @NotBlank String rut,
            @NotBlank String razonSocial,
            @NotBlank @Email String adminEmail,
            @NotBlank String adminPassword) {
    }

    public record LoginRequest(String email, String password) {
    }
}
