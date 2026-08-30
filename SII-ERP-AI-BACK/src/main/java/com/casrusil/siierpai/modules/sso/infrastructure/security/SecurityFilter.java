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

    private final JwtTokenProvider jwtTokenProvider;

    public SecurityFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
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

        // Rutas públicas de autenticación no requieren token
        if (request.getRequestURI().startsWith("/api/v1/auth/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = getTokenFromRequest(request);

        if (token != null && jwtTokenProvider.validateToken(token)) {
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
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
