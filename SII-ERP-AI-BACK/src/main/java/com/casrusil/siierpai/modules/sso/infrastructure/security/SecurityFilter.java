package com.casrusil.siierpai.modules.sso.infrastructure.security;

import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import com.casrusil.siierpai.shared.infrastructure.context.CompanyContext;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * Filtro de Seguridad para autenticación JWT.
 *
 * <p>Intercepta las peticiones HTTP para validar la presencia y corrección
 * del token JWT en el encabezado Authorization.
 *
 * <p>Si el token es válido:
 * <ul>
 * <li>Extrae la identidad del usuario, empresa y rol.</li>
 * <li>Configura el contexto de seguridad de Spring con las authorities reales
 *     (necesario para que {@code @PreAuthorize} funcione correctamente).</li>
 * <li>Inicializa el contexto de empresa (CompanyContext) para multi-tenancy.</li>
 * </ul>
 *
 * @see JwtTokenProvider
 * @see CompanyContext
 * @since 1.0
 */
@Component
public class SecurityFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(SecurityFilter.class);

    /** Nombre de la cookie httpOnly que transporta el JWT (flujo basado en cookie). */
    public static final String ACCESS_TOKEN_COOKIE = "access_token";

    /**
     * Rutas de autenticación públicas. {@code /me} no está acá: requiere JWT.
     */
    private static final Set<String> PUBLIC_AUTH_PATHS = Set.of(
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/auth/logout",
            "/api/v1/auth/invitations/validate",
            "/api/v1/auth/invitations/accept",
            "/auth/login",
            "/auth/register",
            "/auth/logout");

    private final JwtTokenProvider jwtTokenProvider;
    private final TokenBlacklistService tokenBlacklistService;

    public SecurityFilter(JwtTokenProvider jwtTokenProvider, TokenBlacklistService tokenBlacklistService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        // Pasar directamente las peticiones OPTIONS (CORS pre-flight)
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        // Rutas públicas de autenticación no requieren token.
        // /api/v1/auth/me sí requiere JWT, por eso no se incluye acá.
        if (PUBLIC_AUTH_PATHS.contains(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = getTokenFromRequest(request);

        if (token != null && jwtTokenProvider.validateToken(token) && !tokenBlacklistService.isBlacklisted(token)) {
            try {
                Claims claims = jwtTokenProvider.getClaims(token);
                CompanyId companyId = jwtTokenProvider.getCompanyIdFromToken(token);

                // Extraer el rol del token JWT y construir la authority de Spring Security.
                // Esto habilita @PreAuthorize("hasRole('ADMIN')") y similares.
                String role = claims.get("role", String.class);
                List<SimpleGrantedAuthority> authorities = (role != null)
                        ? List.of(new SimpleGrantedAuthority("ROLE_" + role))
                        : List.of();

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                jwtTokenProvider.getUserIdFromToken(token), null, authorities);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);

                // Inicializar CompanyContext con ScopedValue para multi-tenancy thread-safe
                ScopedValue.where(CompanyContext.COMPANY_ID, companyId)
                        .run(() -> {
                            try {
                                filterChain.doFilter(request, response);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        });
                return;

            } catch (Exception e) {
                log.warn("Error procesando token JWT en {}: {}", request.getRequestURI(), e.getMessage());
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extrae el token JWT del header Authorization (formato: "Bearer {token}").
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        // 1. Preferir el header Authorization (clientes de API, integraciones).
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        // 2. Fallback a la cookie httpOnly (flujo del navegador/SPA).
        if (request.getCookies() != null) {
            for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
                if (ACCESS_TOKEN_COOKIE.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
