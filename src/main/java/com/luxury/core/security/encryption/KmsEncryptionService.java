package com.luxury.core.security.encryption;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Application-level encryption service using AES-256-GCM.
 *
 * <p>Provides symmetric encryption/decryption for Phantom Enclave data.
 * In production, the encryption key would be sourced from Azure Key Vault
 * (or equivalent KMS). For local development, a key is read from
 * application properties.</p>
 *
 * <p>Thread-safe: each encrypt/decrypt call generates its own IV and
 * cipher instance.</p>
 */
@Slf4j
@Service
public class KmsEncryptionService {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;   // 96-bit IV (NIST recommended)
    private static final int GCM_TAG_LENGTH = 128;  // 128-bit auth tag

    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${luxury.security.encryption.key}")
    private String encryptionKeyBase64;

    private SecretKey secretKey;

    /**
     * Initializes the AES secret key from the configured property.
     * The key must be exactly 32 bytes (256 bits) for AES-256.
     */
    @PostConstruct
    public void init() {
        byte[] keyBytes = encryptionKeyBase64.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length != 32) {
            throw new IllegalStateException(
                    "Encryption key must be exactly 32 bytes (256 bits). Got: " + keyBytes.length);
        }
        this.secretKey = new SecretKeySpec(keyBytes, "AES");
        log.info("KMS Encryption Service initialized with AES-256-GCM");
    }

    /**
     * Encrypts plaintext using AES-256-GCM.
     *
     * <p>The output format is: Base64( IV || ciphertext || authTag )</p>
     *
     * @param plaintext the data to encrypt
     * @return Base64-encoded ciphertext (IV prepended)
     * @throws EncryptionException if encryption fails
     */
    public String encrypt(String plaintext) {
        if (plaintext == null) {
            return null;
        }

        try {
            byte[] iv = new byte[GCM_IV_LENGTH];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec paramSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, paramSpec);

            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            // Prepend IV to ciphertext: [IV (12 bytes)] + [ciphertext + authTag]
            byte[] combined = new byte[iv.length + ciphertext.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(ciphertext, 0, combined, iv.length, ciphertext.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            throw new EncryptionException("Failed to encrypt Phantom Enclave data", e);
        }
    }

    /**
     * Decrypts Base64-encoded AES-256-GCM ciphertext.
     *
     * @param ciphertext Base64-encoded string (IV prepended)
     * @return the original plaintext
     * @throws EncryptionException if decryption fails
     */
    public String decrypt(String ciphertext) {
        if (ciphertext == null) {
            return null;
        }

        try {
            byte[] combined = Base64.getDecoder().decode(ciphertext);

            // Extract IV and ciphertext
            byte[] iv = new byte[GCM_IV_LENGTH];
            byte[] encrypted = new byte[combined.length - GCM_IV_LENGTH];
            System.arraycopy(combined, 0, iv, 0, iv.length);
            System.arraycopy(combined, iv.length, encrypted, 0, encrypted.length);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec paramSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, paramSpec);

            byte[] plaintext = cipher.doFinal(encrypted);
            return new String(plaintext, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new EncryptionException("Failed to decrypt Phantom Enclave data", e);
        }
    }

    /**
     * Runtime exception for encryption/decryption failures.
     */
    public static class EncryptionException extends RuntimeException {
        public EncryptionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
