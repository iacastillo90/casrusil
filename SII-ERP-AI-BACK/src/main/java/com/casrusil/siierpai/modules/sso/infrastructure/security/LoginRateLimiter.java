package com.casrusil.siierpai.modules.sso.infrastructure.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Limitador de intentos de autenticación (anti fuerza-bruta) respaldado por Redis.
 *
 * <p>Cuenta los intentos fallidos por identificador (p. ej. IP del cliente) dentro
 * de una ventana de tiempo. Superado el máximo, el identificador queda bloqueado
 * hasta que expira la ventana. Un login exitoso resetea el contador.
 *
 * <p><b>Política de fallo:</b> si Redis no está disponible, hace <em>fail-open</em>
 * (no bloquea) para no impedir el acceso legítimo por una caída de la caché.
 *
 * @since 1.0 (Fase 2 — Auth/Authz)
 */
@Service
public class LoginRateLimiter {

    private static final Logger log = LoggerFactory.getLogger(LoginRateLimiter.class);
    private static final String KEY_PREFIX = "auth:attempts:";

    private final RedisTemplate<String, Object> redisTemplate;
    private final int maxAttempts;
    private final Duration window;

    public LoginRateLimiter(
            RedisTemplate<String, Object> redisTemplate,
            @Value("${security.login.max-attempts:5}") int maxAttempts,
            @Value("${security.login.window-minutes:15}") long windowMinutes) {
        this.redisTemplate = redisTemplate;
        this.maxAttempts = maxAttempts;
        this.window = Duration.ofMinutes(windowMinutes);
    }

    /** @return {@code true} si el identificador superó el número de intentos permitidos. */
    public boolean isBlocked(String identifier) {
        try {
            Object value = redisTemplate.opsForValue().get(key(identifier));
            if (value == null) {
                return false;
            }
            long attempts = Long.parseLong(value.toString());
            return attempts >= maxAttempts;
        } catch (Exception e) {
            log.warn("No se pudo consultar el rate limiter (Redis): {}", e.getMessage());
            return false;
        }
    }

    /** Registra un intento fallido e inicia la ventana de expiración en el primer fallo. */
    public void recordFailedAttempt(String identifier) {
        try {
            String k = key(identifier);
            Long attempts = redisTemplate.opsForValue().increment(k);
            if (attempts != null && attempts == 1L) {
                redisTemplate.expire(k, window);
            }
        } catch (Exception e) {
            log.warn("No se pudo registrar intento fallido en el rate limiter (Redis): {}", e.getMessage());
        }
    }

    /** Limpia el contador tras un login exitoso. */
    public void reset(String identifier) {
        try {
            redisTemplate.delete(key(identifier));
        } catch (Exception e) {
            log.warn("No se pudo resetear el rate limiter (Redis): {}", e.getMessage());
        }
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    private String key(String identifier) {
        return KEY_PREFIX + identifier;
    }
}
