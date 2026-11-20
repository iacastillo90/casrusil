package com.casrusil.siierpai.shared.infrastructure.crypto;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SecretCipherTest {

    private final SecretCipher cipher = new SecretCipher("test-key-para-cifrado");

    @Test
    void shouldEncryptAndDecryptStrings() {
        String plaintext = "contraseña-del-certificado";

        String encrypted = cipher.encrypt(plaintext);

        assertNotEquals(plaintext, encrypted);
        assertNotEquals(encrypted, cipher.encrypt(plaintext), "AES-GCM debe usar IV distinto cada vez");
        assertEquals(plaintext, cipher.decrypt(encrypted));
    }

    @Test
    void shouldEncryptAndDecryptBytes() {
        byte[] plaintext = "PKCS12-BYTES".getBytes(StandardCharsets.UTF_8);

        byte[] encrypted = cipher.encryptBytes(plaintext);

        assertNotEquals(new String(plaintext), new String(encrypted));
        assertArrayEquals(plaintext, cipher.decryptBytes(encrypted));
    }

    @Test
    void shouldKeepNullAsNull() {
        assertNull(cipher.encrypt(null));
        assertNull(cipher.decrypt(null));
    }

    @Test
    void shouldFailFastInProdWithDevelopmentKey() {
        org.springframework.core.env.Environment prod = mock(org.springframework.core.env.Environment.class);
        when(prod.acceptsProfiles(any(org.springframework.core.env.Profiles.class))).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> new SecretCipher("dev-only-insecure-key-change-me", prod));
    }

    @Test
    void shouldAcceptConfiguredKeyInProd() {
        org.springframework.core.env.Environment prod = mock(org.springframework.core.env.Environment.class);
        when(prod.acceptsProfiles(any(org.springframework.core.env.Profiles.class))).thenReturn(true);

        SecretCipher configured = new SecretCipher("clave-real-de-produccion", prod);

        assertEquals("x", configured.decrypt(configured.encrypt("x")));
    }
}
