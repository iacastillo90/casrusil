package com.casrusil.siierpai.shared.infrastructure.cache;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuración de Redis para caché distribuida del SII-ERP.
 *
 * <p>Usos implementados:
 * <ul>
 *   <li><b>indicadores</b>: Respuestas de Mindicador.cl (UTM, UF, Dólar) — TTL: 1 hora</li>
 *   <li><b>invoices</b>: Listado de facturas por empresa — TTL: 5 minutos</li>
 *   <li><b>default</b>: Caché genérica — TTL: 30 minutos</li>
 * </ul>
 *
 * <p>Usos planificados:
 * <ul>
 *   <li>Rate limiting de endpoints de IA (via RedisTemplate directo)</li>
 *   <li>Blacklist de tokens JWT revocados</li>
 * </ul>
 *
 * @since 1.0
 */
@Configuration
@EnableCaching
public class RedisConfig {

    @Value("${spring.redis.host:sii-redis}")
    private String redisHost;

    @Value("${spring.redis.port:6379}")
    private int redisPort;

    @Value("${spring.redis.password:}")
    private String redisPassword;

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration serverConfig = new RedisStandaloneConfiguration(redisHost, redisPort);

        if (redisPassword != null && !redisPassword.isBlank()) {
            serverConfig.setPassword(redisPassword);
        }

        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
                .commandTimeout(Duration.ofSeconds(5))
                .shutdownTimeout(Duration.ZERO)
                .build();

        return new LettuceConnectionFactory(serverConfig, clientConfig);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // Configuración base: JSON serialization + TTL de 30 min por defecto
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues();

        // TTL específico por caché
        Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();

        // Indicadores económicos: cambio cada hora (UTM, UF, dólar, euro)
        cacheConfigs.put("indicadores", defaultConfig.entryTtl(Duration.ofHours(1)));

        // Facturas por empresa: actualización frecuente, TTL corto
        cacheConfigs.put("invoices", defaultConfig.entryTtl(Duration.ofMinutes(5)));

        // Productos del inventario por empresa
        cacheConfigs.put("products", defaultConfig.entryTtl(Duration.ofMinutes(10)));

        // Respuestas de IA (para evitar llamadas duplicadas)
        cacheConfigs.put("ai-responses", defaultConfig.entryTtl(Duration.ofHours(24)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigs)
                .transactionAware()
                .build();
    }
}
