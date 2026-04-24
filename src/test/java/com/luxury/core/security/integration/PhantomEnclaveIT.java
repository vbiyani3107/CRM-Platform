package com.luxury.core.security.integration;

import com.luxury.core.security.encryption.KmsEncryptionService;
import com.luxury.core.security.service.SecurityContextService;
import com.luxury.core.persistence.integration.AbstractPostgresIT;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for Phantom Enclave (application-level encryption).
 * Uses Testcontainers with pgvector/pgvector:pg16 to verify the
 * encryption service works with the full Spring context.
 */
@SpringBootTest(properties = {
        "luxury.security.encryption.key=test-only-32-byte-key-for-unit!!",
        "luxury.security.fls.enabled=true"
})
@DisplayName("Phantom Enclave — Integration Tests")
class PhantomEnclaveIT extends AbstractPostgresIT {

    @Autowired
    private KmsEncryptionService kmsEncryptionService;

    @Autowired
    private SecurityContextService securityContextService;

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateWithRoles(String... roles) {
        Jwt jwt = Jwt.withTokenValue("test-token")
                .header("alg", "RS256")
                .claim("preferred_username", "phantom@luxury.com")
                .claim("sub", "phantom-sub")
                .claim("roles", List.of(roles))
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        List<SimpleGrantedAuthority> authorities = List.of(roles).stream()
                .map(SimpleGrantedAuthority::new)
                .toList();

        SecurityContextHolder.getContext().setAuthentication(
                new JwtAuthenticationToken(jwt, authorities));
    }

    @Test
    @DisplayName("Should encrypt and decrypt data successfully in Spring context")
    void shouldEncryptAndDecryptInSpringContext() {
        String sensitiveData = "Client's offshore portfolio — $50M in Swiss trusts";

        String encrypted = kmsEncryptionService.encrypt(sensitiveData);
        assertThat(encrypted).isNotEqualTo(sensitiveData);
        assertThat(encrypted).isNotBlank();

        String decrypted = kmsEncryptionService.decrypt(encrypted);
        assertThat(decrypted).isEqualTo(sensitiveData);
    }

    @Test
    @DisplayName("Should verify encrypted data is not plaintext in storage format")
    void shouldVerifyEncryptedIsNotPlaintext() {
        String notes = "Paramour relationship — requires absolute discretion";

        String encrypted = kmsEncryptionService.encrypt(notes);

        // The encrypted output should not contain recognizable plaintext
        assertThat(encrypted).doesNotContain("Paramour");
        assertThat(encrypted).doesNotContain("discretion");
        assertThat(encrypted).doesNotContain("relationship");
    }

    @Test
    @DisplayName("Should verify Phantom Clearance role is correctly detected")
    void shouldVerifyPhantomClearanceDetection() {
        authenticateWithRoles("ROLE_PHANTOM_CLEARANCE");
        assertThat(securityContextService.hasPhantomClearance()).isTrue();
    }

    @Test
    @DisplayName("Should verify non-Phantom user cannot access clearance")
    void shouldVerifyNonPhantomUserLacksClearance() {
        authenticateWithRoles("ROLE_VIP_DIRECTOR");
        assertThat(securityContextService.hasPhantomClearance()).isFalse();
    }

    @Test
    @DisplayName("Should handle multiple encrypt/decrypt cycles")
    void shouldHandleMultipleCycles() {
        String[] secrets = {
                "First secret — diamond provenance",
                "Second secret — acquisition network",
                "Third secret — family trust structure"
        };

        for (String secret : secrets) {
            String encrypted = kmsEncryptionService.encrypt(secret);
            String decrypted = kmsEncryptionService.decrypt(encrypted);
            assertThat(decrypted).isEqualTo(secret);
        }
    }
}
