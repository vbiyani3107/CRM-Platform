package com.luxury.core.security.encryption;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link KmsEncryptionService}.
 * Verifies AES-256-GCM encrypt/decrypt round-trip, null handling,
 * and key validation.
 */
@DisplayName("KmsEncryptionService — Unit Tests")
class KmsEncryptionServiceTest {

    private KmsEncryptionService service;

    @BeforeEach
    void setUp() {
        service = new KmsEncryptionService();
        // Must be exactly 32 bytes for AES-256
        ReflectionTestUtils.setField(service, "encryptionKeyBase64", "test-only-32-byte-key-for-unit!!");
        service.init();
    }

    @Nested
    @DisplayName("Encrypt/Decrypt Round-Trip")
    class RoundTrip {

        @Test
        @DisplayName("Should encrypt and decrypt a simple string")
        void shouldRoundTrip() {
            String plaintext = "Client prefers platinum over gold.";

            String encrypted = service.encrypt(plaintext);
            String decrypted = service.decrypt(encrypted);

            assertThat(decrypted).isEqualTo(plaintext);
        }

        @Test
        @DisplayName("Should produce different ciphertexts for the same plaintext (unique IV)")
        void shouldProduceDifferentCiphertexts() {
            String plaintext = "Ultra-sensitive offshore network details.";

            String encrypted1 = service.encrypt(plaintext);
            String encrypted2 = service.encrypt(plaintext);

            assertThat(encrypted1).isNotEqualTo(encrypted2);

            // Both should decrypt to the same value
            assertThat(service.decrypt(encrypted1)).isEqualTo(plaintext);
            assertThat(service.decrypt(encrypted2)).isEqualTo(plaintext);
        }

        @Test
        @DisplayName("Should handle long text")
        void shouldHandleLongText() {
            String plaintext = "A".repeat(10_000);

            String encrypted = service.encrypt(plaintext);
            String decrypted = service.decrypt(encrypted);

            assertThat(decrypted).isEqualTo(plaintext);
        }

        @Test
        @DisplayName("Should handle Unicode text")
        void shouldHandleUnicode() {
            String plaintext = "Клиент предпочитает 白金 über alles — 🏆";

            String encrypted = service.encrypt(plaintext);
            String decrypted = service.decrypt(encrypted);

            assertThat(decrypted).isEqualTo(plaintext);
        }

        @Test
        @DisplayName("Should handle empty string")
        void shouldHandleEmptyString() {
            String encrypted = service.encrypt("");
            String decrypted = service.decrypt(encrypted);

            assertThat(decrypted).isEmpty();
        }
    }

    @Nested
    @DisplayName("Null Handling")
    class NullHandling {

        @Test
        @DisplayName("Should return null when encrypting null")
        void shouldReturnNullForNullEncrypt() {
            assertThat(service.encrypt(null)).isNull();
        }

        @Test
        @DisplayName("Should return null when decrypting null")
        void shouldReturnNullForNullDecrypt() {
            assertThat(service.decrypt(null)).isNull();
        }
    }

    @Nested
    @DisplayName("Key Validation")
    class KeyValidation {

        @Test
        @DisplayName("Should reject key shorter than 32 bytes")
        void shouldRejectShortKey() {
            KmsEncryptionService badService = new KmsEncryptionService();
            ReflectionTestUtils.setField(badService, "encryptionKeyBase64", "too-short");

            assertThatThrownBy(badService::init)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("32 bytes");
        }

        @Test
        @DisplayName("Should reject key longer than 32 bytes")
        void shouldRejectLongKey() {
            KmsEncryptionService badService = new KmsEncryptionService();
            ReflectionTestUtils.setField(badService, "encryptionKeyBase64", "this-key-is-way-too-long-for-aes-256-and-should-fail");

            assertThatThrownBy(badService::init)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("32 bytes");
        }
    }

    @Nested
    @DisplayName("Tamper Detection")
    class TamperDetection {

        @Test
        @DisplayName("Should fail to decrypt tampered ciphertext")
        void shouldFailOnTamperedCiphertext() {
            String encrypted = service.encrypt("secret data");

            // Tamper with the base64 string by flipping a character
            char[] chars = encrypted.toCharArray();
            chars[20] = (chars[20] == 'A') ? 'B' : 'A';
            String tampered = new String(chars);

            assertThatThrownBy(() -> service.decrypt(tampered))
                    .isInstanceOf(KmsEncryptionService.EncryptionException.class);
        }
    }
}
