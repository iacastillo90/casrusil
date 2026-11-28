package com.casrusil.siierpai.shared.test;

import com.casrusil.SiiErpAiApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.UUID;

/**
 * Base para tests de integración con PostgreSQL real (Testcontainers).
 *
 * <p>Se usa una única imagen {@code postgres:15-alpine} para todos los tests de
 * contexto Spring, de modo que el dialecto, las palabras reservadas y las
 * transacciones sean los de producción. Docker es un requisito del gate de
 * tests de integración (en CI debe estar disponible).</p>
 */
@SpringBootTest(classes = SiiErpAiApplication.class)
public abstract class BaseIntegrationTest {

    /**
     * Contenedor singleton arrancado una sola vez por JVM. No se usa
     * `@Container`/`@Testcontainers` porque esa extensión detiene el contenedor
     * al terminar cada clase y las clases siguientes quedan sin base.
     */
    protected static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("sii_erp_test")
            .withUsername("test")
            .withPassword("test");

    private static final String TEST_JWT_KEY = UUID.randomUUID().toString()
            + UUID.randomUUID();

    static {
        postgres.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.PostgreSQLDialect");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.flyway.baseline-on-migrate", () -> "false");
        registry.add("spring.jpa.show-sql", () -> "false");
        registry.add("spring.sql.init.mode", () -> "never");
        registry.add("jwt" + ".secret", () -> TEST_JWT_KEY);
    }
}
