package com.casrusil.siierpai.modules.integration_sii.infrastructure.crypto;

import com.casrusil.siierpai.modules.integration_sii.domain.model.SiiCertificate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Fixture: {@code src/test/resources/fixtures/dummy.p12} es un PKCS#12
 * autofirmado generado con keytool (password: "password"), sin datos reales.
 */
class Pkcs12HandlerTest {

    private static final String FIXTURE = "/fixtures/dummy.p12";
    private static final String PASSWORD = "password";

    private Pkcs12Handler pkcs12Handler;

    @BeforeEach
    void setUp() {
        pkcs12Handler = new Pkcs12Handler();
    }

    @Test
    void loadCertificate_ShouldLoadValidCertificate() throws Exception {
        try (InputStream is = getClass().getResourceAsStream(FIXTURE)) {
            assertNotNull(is, "No se encontró el fixture " + FIXTURE);

            SiiCertificate certificate = pkcs12Handler.loadCertificate(is, PASSWORD);

            assertNotNull(certificate);
            assertNotNull(certificate.certificate());
            assertNotNull(certificate.privateKey());
            assertEquals("UNKNOWN-RUT", certificate.rut());
        }
    }

    @Test
    void loadCertificate_ShouldThrowException_WhenPasswordIsIncorrect() throws Exception {
        try (InputStream is = getClass().getResourceAsStream(FIXTURE)) {
            assertNotNull(is, "No se encontró el fixture " + FIXTURE);
            assertThrows(RuntimeException.class, () -> pkcs12Handler.loadCertificate(is, "wrongpassword"));
        }
    }

    @Test
    void loadCertificate_ShouldThrowException_WhenFileDoesNotExist() {
        assertThrows(RuntimeException.class,
                () -> pkcs12Handler.loadCertificate("non-existent-file.p12", PASSWORD));
    }
}
