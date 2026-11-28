package com.casrusil.siierpai.modules.sso.infrastructure.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HexFormat;

/**
 * Servicio de revocación de tokens JWT (blacklist) respaldado por Redis.
 *
 * <p>Como los JWT son stateless, el logout "real" requiere marcar el token como
 * revocado hasta su expiración natural. Guardamos el <em>hash</em> del token
 * (no el token en claro) con un TTL igual al tiempo restante de vida del token,
 * de modo que Redis lo purga automáticamente cuando ya no puede ser válido.
 *
 * <p><b>Política de fallo:</b> si Redis no está disponible, {@link #isBlacklisted}
 * hace <em>fail-open</em> (registra un warning y permite continuar) para no dejar
 * el sistema sin autenticación por una caída de la caché. La revocación es una
 * defensa adicional; la validez criptográfica del token la garantiza la firma.
 *
 * @since 1.0 (Fase 2 — Auth/Authz)
 */
@Service
public class TokenBlacklistService {

    private static final Logger log = LoggerFactory.getLogger(TokenBlacklistService.class);
    private static final String KEY_PREFIX = "jwt:blacklist:";
    private static final String REVOKED = "revoked";

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * B04: en prod ({@code security.redis.fail-closed=true}) un logout que no puede
     * revocar el token en Redis se rechaza con 503 en vez de fingir éxito. En dev
     * sigue siendo permisivo porque Redis puede no estar levantado.
     */
    private final boolean failClosed;

    public TokenBlacklistService(RedisTemplate<String, Object> redisTemplate,
            @org.springframework.beans.factory.annotation.Value("${security.redis.fail-closed:false}") boolean failClosed) {
        this.redisTemplate = redisTemplate;
        this.failClosed = failClosed;
    }

    /**
     * Revoca un token hasta su fecha de expiración.
     *
     * @param token       el JWT a revocar
     * @param expiration  fecha de expiración del token (para calcular el TTL)
     */
    public void blacklist(String token, Date expiration) {
        try {
            long ttlMillis = (expiration != null)
                    ? expiration.getTime() - Instant.now().toEpochMilli()
                    : 0L;
            if (ttlMillis <= 0) {
                return; // ya expirado: no hace falta almacenarlo
            }
            redisTemplate.opsForValue().set(key(token), REVOKED, Duration.ofMillis(ttlMillis));
        } catch (Exception e) {
            if (failClosed) {
                log.error("Logout rechazado: no se pudo revocar el token en Redis: {}", e.getMessage());
                throw new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE,
                        "No se pudo cerrar la sesión de forma segura (servicio de sesiones no disponible). "
                                + "Intenta nuevamente en unos minutos.");
            }
            log.warn("No se pudo registrar el token en la blacklist (Redis): {}", e.getMessage());
        }
    }

    /**
     * @return {@code true} si el token fue revocado; {@code false} si no lo está
     *         o si Redis no está disponible (fail-open).
     */
    public boolean isBlacklisted(String token) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(key(token)));
        } catch (Exception e) {
            log.warn("No se pudo consultar la blacklist de tokens (Redis): {}", e.getMessage());
            return false;
        }
    }

    private String key(String token) {
        return KEY_PREFIX + sha256(token);
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 siempre está disponible en la JVM; fallback defensivo.
            return Integer.toHexString(value.hashCode());
        }
    }
}
