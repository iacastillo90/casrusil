package com.casrusil.siierpai.modules.sso.infrastructure.security;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * B04/B05: con Redis caído, en prod ({@code security.redis.fail-closed=true}) el logout
 * y el rate limiter de login rechazan con 503; en dev (false) siguen permisivos.
 */
class RedisFailModeTest {

    private static final Date IN_ONE_HOUR = new Date(System.currentTimeMillis() + 3_600_000);

    @SuppressWarnings("unchecked")
    private static RedisTemplate<String, Object> redisDown() {
        RedisTemplate<String, Object> template = mock(RedisTemplate.class);
        ValueOperations<String, Object> ops = mock(ValueOperations.class);
        RedisConnectionFailureException down = new RedisConnectionFailureException("Redis caído");
        when(template.opsForValue()).thenReturn(ops);
        when(ops.get(anyString())).thenThrow(down);
        when(ops.increment(anyString())).thenThrow(down);
        doThrow(down).when(ops).set(anyString(), any(), any(Duration.class));
        when(template.hasKey(anyString())).thenThrow(down);
        return template;
    }

    @Test
    void prod_logoutSinRedis_rechazaCon503() {
        TokenBlacklistService service = new TokenBlacklistService(redisDown(), true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.blacklist("jwt", IN_ONE_HOUR));
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, ex.getStatusCode());
        assertTrue(ex.getReason().contains("cerrar la sesión"));
    }

    @Test
    void dev_logoutSinRedis_esPermisivo() {
        TokenBlacklistService service = new TokenBlacklistService(redisDown(), false);

        assertDoesNotThrow(() -> service.blacklist("jwt", IN_ONE_HOUR));
        assertFalse(service.isBlacklisted("jwt"));
    }

    @Test
    void prod_rateLimiterSinRedis_rechazaLoginCon503() {
        LoginRateLimiter limiter = new LoginRateLimiter(redisDown(), 5, 15, true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> limiter.isBlocked("10.0.0.1"));
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, ex.getStatusCode());
        assertTrue(ex.getReason().contains("inicio de sesión"));
    }

    @Test
    void dev_rateLimiterSinRedis_esPermisivo() {
        LoginRateLimiter limiter = new LoginRateLimiter(redisDown(), 5, 15, false);

        assertFalse(limiter.isBlocked("10.0.0.1"));
        assertDoesNotThrow(() -> limiter.recordFailedAttempt("10.0.0.1"));
    }
}
