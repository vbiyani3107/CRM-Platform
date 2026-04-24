package com.luxury.core.security.encryption;

import com.luxury.core.security.service.SecurityContextService;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * JPA {@link AttributeConverter} for Phantom Enclave data.
 *
 * <p>Encrypts data on persist and conditionally decrypts on read based on
 * the current user's clearance level. Associates without
 * {@code ROLE_PHANTOM_CLEARANCE} see a redacted placeholder instead of
 * the actual decrypted value.</p>
 *
 * <p>Applied to entity fields via {@code @Convert(converter = PhantomEnclaveConverter.class)}.</p>
 *
 * @see KmsEncryptionService
 * @see SecurityContextService
 */
@Slf4j
@Converter
@Component
public class PhantomEnclaveConverter implements AttributeConverter<String, String> {

    private static final String REDACTED_PLACEHOLDER =
            "[ENCRYPTED ENCLAVE DATA - CLEARANCE REQUIRED]";

    @Autowired
    private KmsEncryptionService kmsEncryptionService;

    @Autowired
    private SecurityContextService securityContextService;

    /**
     * Encrypts the entity attribute value before writing to the database.
     * All Phantom Enclave data is stored as ciphertext regardless of clearance.
     *
     * @param rawValue the plaintext attribute value
     * @return Base64-encoded AES-256-GCM ciphertext, or null if input is null
     */
    @Override
    public String convertToDatabaseColumn(String rawValue) {
        if (rawValue == null) {
            return null;
        }
        log.debug("Encrypting Phantom Enclave data for database storage");
        return kmsEncryptionService.encrypt(rawValue);
    }

    /**
     * Conditionally decrypts the entity attribute value when reading from the database.
     *
     * <p>Only users with {@code ROLE_PHANTOM_CLEARANCE} receive the decrypted value.
     * All other users receive a redacted placeholder string.</p>
     *
     * @param cipherText the Base64-encoded ciphertext from the database
     * @return decrypted plaintext if clearance is held, or a redacted placeholder
     */
    @Override
    public String convertToEntityAttribute(String cipherText) {
        if (cipherText == null) {
            return null;
        }

        if (securityContextService.hasPhantomClearance()) {
            log.debug("Phantom Clearance verified — decrypting enclave data");
            return kmsEncryptionService.decrypt(cipherText);
        }

        log.debug("Phantom Clearance NOT held — returning redacted placeholder");
        return REDACTED_PLACEHOLDER;
    }
}
