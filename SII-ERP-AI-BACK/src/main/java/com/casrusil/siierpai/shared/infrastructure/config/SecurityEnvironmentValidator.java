package com.casrusil.siierpai.shared.infrastructure.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import java.util.Arrays;

@Configuration
public class SecurityEnvironmentValidator {

    private static final Logger logger = LoggerFactory.getLogger(SecurityEnvironmentValidator.class);

    @Value("${jwt.secret:__GENERAR_CON_openssl_rand_hex_64__}")
    private String jwtSecret;

    @Value("${langchain4j.google-ai-gemini.chat-model.api-key:__CAMBIAR__}")
    private String geminiApiKey;

    @Value("${ai.openai.api-key:__CAMBIAR__}")
    private String openaiApiKey;

    private final Environment environment;

    public SecurityEnvironmentValidator(Environment environment) {
        this.environment = environment;
    }

    @PostConstruct
    public void validateSecrets() {
        boolean isProd = Arrays.asList(environment.getActiveProfiles()).contains("prod");

        if (isProd) {
            logger.info("Validating environment secrets for production...");
            if ("__GENERAR_CON_openssl_rand_hex_64__".equals(jwtSecret)) {
                throw new IllegalStateException("CRITICAL SECURITY ERROR: JWT_SECRET has not been configured for production.");
            }
            if ("__CAMBIAR__".equals(geminiApiKey) || "__CAMBIAR__".equals(openaiApiKey)) {
                logger.warn("SECURITY WARNING: AI API Keys have not been configured. AI functionalities will fail.");
            }
        }
    }
}
