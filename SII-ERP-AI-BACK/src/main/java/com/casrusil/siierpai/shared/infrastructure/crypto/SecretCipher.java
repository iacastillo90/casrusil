package com.casrusil.siierpai.shared.infrastructure.crypto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

/**
 * Cifra secretos en reposo con AES-GCM.
 *
 * <p>
 * La clave se toma de {@code app.crypto.key}. En desarrollo hay un valor por
 * defecto (se registra un warning); en producción debe configurarse.
 * </p>
 *
 * <p>
 * Formato del texto cifrado: {@code base64(iv[12] || ciphertext+tag)}.
 * </p>
 */
@Component
public class SecretCipher {

    private static final Logger log = LoggerFactory.getLogger(SecretCipher.class);
    private static final String DEV_DEFAULT_KEY = "dev-only-insecure-key-change-me";
    private static final int IV_LENGTH = 12;
    private static final int TAG_BITS = 128;

    private final SecretKeySpec key;
    private final SecureRandom random = new SecureRandom();

    public SecretCipher(@Value("${app.crypto.key:" + DEV_DEFAULT_KEY + "}") String rawKey) {
        if (DEV_DEFAULT_KEY.equals(rawKey)) {
            log.warn("app.crypto.key no configurada: se usa la clave de desarrollo. No usar en producción.");
        }
        this.key = new SecretKeySpec(sha256(rawKey), "AES");
    }

    public String encrypt(String plaintext) {
        if (plaintext == null) {
            return null;
        }
        try {
            byte[] iv = new byte[IV_LENGTH];
            random.nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, iv));
            byte[] encrypted = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            byte[] payload = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, payload, 0, iv.length);
            System.arraycopy(encrypted, 0, payload, iv.length, encrypted.length);
            return Base64.getEncoder().encodeToString(payload);
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo cifrar el secreto", e);
        }
    }

    public String decrypt(String ciphertext) {
        if (ciphertext == null) {
            return null;
        }
        try {
            byte[] payload = Base64.getDecoder().decode(ciphertext);
            byte[] iv = Arrays.copyOfRange(payload, 0, IV_LENGTH);
            byte[] encrypted = Arrays.copyOfRange(payload, IV_LENGTH, payload.length);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, iv));
            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo descifrar el secreto", e);
        }
    }

    public byte[] encryptBytes(byte[] plaintext) {
        if (plaintext == null) {
            return null;
        }
        return Base64.getDecoder().decode(encrypt(Base64.getEncoder().encodeToString(plaintext)));
    }

    public byte[] decryptBytes(byte[] ciphertext) {
        if (ciphertext == null) {
            return null;
        }
        return Base64.getDecoder().decode(decrypt(Base64.getEncoder().encodeToString(ciphertext)));
    }

    private static byte[] sha256(String value) {
        try {
            return MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }
}
