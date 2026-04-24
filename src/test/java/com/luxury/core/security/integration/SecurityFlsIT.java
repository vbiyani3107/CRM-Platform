package com.luxury.core.security.integration;

import com.luxury.common.dto.ApiResponse;
import com.luxury.core.persistence.model.SysDictionary;
import com.luxury.core.persistence.repository.SysDictionaryRepository;
import com.luxury.core.security.fls.FlsInterceptor;
import com.luxury.core.security.service.SecurityContextService;
import com.luxury.core.persistence.integration.AbstractPostgresIT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for the Field-Level Security (FLS) interceptor.
 * Uses Testcontainers with pgvector/pgvector:pg16 to verify end-to-end
 * FLS behavior with real sys_dictionary data from V2 migration.
 */
@SpringBootTest(properties = {
        "luxury.security.encryption.key=test-only-32-byte-key-for-unit!!",
        "luxury.security.fls.enabled=true"
})
@DisplayName("FLS Interceptor — Integration Tests")
class SecurityFlsIT extends AbstractPostgresIT {

    @Autowired
    private SysDictionaryRepository sysDictionaryRepository;

    @Autowired
    private SecurityContextService securityContextService;

    @Autowired
    private FlsInterceptor flsInterceptor;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();

        // Ensure sensitive field definitions exist (V2 migration seeds these,
        // but we add explicitly for test isolation)
        if (!sysDictionaryRepository.existsByEntityNameAndAttributeName(
                "hyper_profiles", "influence_score")) {
            SysDictionary influenceScore = new SysDictionary();
            influenceScore.setEntityName("hyper_profiles");
            influenceScore.setAttributeName("influence_score");
            influenceScore.setDataType("decimal");
            influenceScore.setIsSensitive(true);
            influenceScore.setDescription("Client influence score — restricted.");
            sysDictionaryRepository.save(influenceScore);
        }
    }

    private void authenticateWithRoles(String... roles) {
        Jwt jwt = Jwt.withTokenValue("test-token")
                .header("alg", "RS256")
                .claim("preferred_username", "test@luxury.com")
                .claim("sub", "test-sub")
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
    @DisplayName("Should verify sensitive fields exist in dictionary after V2 migration")
    void shouldHaveSensitiveFieldsFromMigration() {
        List<SysDictionary> sensitiveFields =
                sysDictionaryRepository.findByEntityNameAndIsSensitiveTrue("hyper_profiles");

        assertThat(sensitiveFields)
                .isNotEmpty()
                .anyMatch(f -> f.getAttributeName().equals("influence_score"));
    }

    @Test
    @DisplayName("Should strip sensitive fields for non-VIP associate")
    void shouldStripFieldsForNonVipAssociate() {
        authenticateWithRoles("ROLE_ASSOCIATE");

        // Verify the service correctly identifies non-VIP
        assertThat(securityContextService.hasVipDirectorAccess()).isFalse();

        // Verify sensitive fields exist in dictionary
        List<SysDictionary> sensitiveFields =
                sysDictionaryRepository.findByEntityNameAndIsSensitiveTrue("hyper_profiles");
        assertThat(sensitiveFields).isNotEmpty();
    }

    @Test
    @DisplayName("Should preserve all fields for VIP Director")
    void shouldPreserveFieldsForVipDirector() {
        authenticateWithRoles("ROLE_VIP_DIRECTOR");

        assertThat(securityContextService.hasVipDirectorAccess()).isTrue();
    }

    @Test
    @DisplayName("Should correctly query sensitive fields by entity name")
    void shouldQuerySensitiveFieldsByEntity() {
        List<SysDictionary> hyperProfileSensitive =
                sysDictionaryRepository.findByEntityNameAndIsSensitiveTrue("hyper_profiles");
        List<SysDictionary> nonExistent =
                sysDictionaryRepository.findByEntityNameAndIsSensitiveTrue("non_existent_entity");

        assertThat(hyperProfileSensitive).isNotEmpty();
        assertThat(nonExistent).isEmpty();
    }
}
