package com.casrusil.siierpai.modules.sso.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Filtro de Rate Limiting para proteger endpoints de autenticación contra fuerza bruta.
 * Implementa un algoritmo simple tipo Token Bucket en memoria (o Map de contadores).
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    // Guarda IP -> [Intentos, Timestamp del primer intento en la ventana]
    private final Map<String, RateLimitRecord> requestCounts = new ConcurrentHashMap<>();

    private static final int MAX_REQUESTS_PER_MINUTE = 10;
    private static final long WINDOW_TIME_MS = 60000; // 1 minuto

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // Solo aplicar rate limiting a endpoints de login/auth
        if (request.getRequestURI().startsWith("/api/v1/auth/")) {
            String clientIp = getClientIp(request);
            
            RateLimitRecord record = requestCounts.compute(clientIp, (key, value) -> {
                long now = Instant.now().toEpochMilli();
                if (value == null || (now - value.windowStart) > WINDOW_TIME_MS) {
                    // Nuevo registro o ventana expirada
                    return new RateLimitRecord(1, now);
                } else {
                    // Incrementar en la ventana actual
                    value.count++;
                    return value;
                }
            });

            if (record.count > MAX_REQUESTS_PER_MINUTE) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"Too many requests. Please try again later.\"}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty()) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }

    private static class RateLimitRecord {
        int count;
        long windowStart;

        RateLimitRecord(int count, long windowStart) {
            this.count = count;
            this.windowStart = windowStart;
        }
    }
}
