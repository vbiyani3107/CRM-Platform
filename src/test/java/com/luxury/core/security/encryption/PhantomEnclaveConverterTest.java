package com.luxury.core.security.encryption;

import com.luxury.core.security.service.SecurityContextService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link PhantomEnclaveConverter}.
 * Mocks both {@link KmsEncryptionService} and {@link SecurityContextService}
 * to verify the converter's role-gated encrypt/decrypt behavior.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PhantomEnclaveConverter — Unit Tests")
class PhantomEnclaveConverterTest {

    @Mock
    private KmsEncryptionService kmsEncryptionService;

    @Mock
    private SecurityContextService securityContextService;

    @InjectMocks
    private PhantomEnclaveConverter converter;

    private static final String PLAINTEXT = "Client's offshore holdings structure — highly confidential.";
    private static final String CIPHERTEXT = "BASE64_ENCRYPTED_DATA_HERE";
    private static final String REDACTED = "[ENCRYPTED ENCLAVE DATA - CLEARANCE REQUIRED]";

    // ─── convertToDatabaseColumn ─────────────────────────────────────────────

    @Nested
    @DisplayName("convertToDatabaseColumn()")
    class ConvertToDatabase {

        @Test
        @DisplayName("Should encrypt plaintext before storing in database")
        void shouldEncryptOnPersist() {
            when(kmsEncryptionService.encrypt(PLAINTEXT)).thenReturn(CIPHERTEXT);

            String result = converter.convertToDatabaseColumn(PLAINTEXT);

            assertThat(result).isEqualTo(CIPHERTEXT);
            verify(kmsEncryptionService).encrypt(PLAINTEXT);
        }

        @Test
        @DisplayName("Should return null when input is null")
        void shouldReturnNullForNullInput() {
            String result = converter.convertToDatabaseColumn(null);

            assertThat(result).isNull();
            verifyNoInteractions(kmsEncryptionService);
        }
    }

    // ─── convertToEntityAttribute ────────────────────────────────────────────

    @Nested
    @DisplayName("convertToEntityAttribute()")
    class ConvertToEntity {

        @Test
        @DisplayName("Should decrypt ciphertext when user has Phantom Clearance")
        void shouldDecryptWithClearance() {
            when(securityContextService.hasPhantomClearance()).thenReturn(true);
            when(kmsEncryptionService.decrypt(CIPHERTEXT)).thenReturn(PLAINTEXT);

            String result = converter.convertToEntityAttribute(CIPHERTEXT);

            assertThat(result).isEqualTo(PLAINTEXT);
            verify(kmsEncryptionService).decrypt(CIPHERTEXT);
        }

        @Test
        @DisplayName("Should return redacted placeholder when user lacks Phantom Clearance")
        void shouldReturnRedactedWithoutClearance() {
            when(securityContextService.hasPhantomClearance()).thenReturn(false);

            String result = converter.convertToEntityAttribute(CIPHERTEXT);

            assertThat(result).isEqualTo(REDACTED);
            verifyNoInteractions(kmsEncryptionService);
        }

        @Test
        @DisplayName("Should return null when ciphertext is null")
        void shouldReturnNullForNullCiphertext() {
            String result = converter.convertToEntityAttribute(null);

            assertThat(result).isNull();
            verifyNoInteractions(securityContextService);
            verifyNoInteractions(kmsEncryptionService);
        }
    }
}
