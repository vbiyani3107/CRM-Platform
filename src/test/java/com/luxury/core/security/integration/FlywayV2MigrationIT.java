package com.luxury.core.security.integration;

import com.luxury.core.persistence.integration.AbstractPostgresIT;
import com.luxury.core.persistence.model.SysDictionary;
import com.luxury.core.persistence.model.SysDbObject;
import com.luxury.core.persistence.repository.SysDictionaryRepository;
import com.luxury.core.persistence.repository.SysDbObjectRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test verifying that the V2__security_seed.sql Flyway migration
 * runs cleanly after V1__init_foundation.sql and that all security-related
 * seed data is correctly persisted.
 *
 * Uses pgvector/pgvector:pg16 Testcontainer (via AbstractPostgresIT)
 * with Flyway enabled.
 */
@SpringBootTest(properties = {
        "luxury.security.encryption.key=test-only-32-byte-key-for-unit!!",
        "luxury.security.fls.enabled=true"
})
@DisplayName("V2 Security Migration — Integration Tests")
class FlywayV2MigrationIT extends AbstractPostgresIT {

    @Autowired
    private SysDictionaryRepository sysDictionaryRepository;

    @Autowired
    private SysDbObjectRepository sysDbObjectRepository;

    @Test
    @DisplayName("Should have sensitive field definitions from V2 migration")
    void shouldHaveSensitiveFieldDefinitions() {
        List<SysDictionary> sensitiveFields =
                sysDictionaryRepository.findByEntityNameAndIsSensitiveTrue("hyper_profiles");

        assertThat(sensitiveFields)
                .extracting(SysDictionary::getAttributeName)
                .contains("influence_score", "net_worth_band", "wealth_indicators",
                        "concierge_notes", "family_network");
    }

    @Test
    @DisplayName("Should have curated_vault sensitive fields from V2 migration")
    void shouldHaveCuratedVaultSensitiveFields() {
        List<SysDictionary> vaultSensitive =
                sysDictionaryRepository.findByEntityNameAndIsSensitiveTrue("curated_vault");

        assertThat(vaultSensitive)
                .extracting(SysDictionary::getAttributeName)
                .contains("acquisition_price", "provenance_notes");
    }

    @Test
    @DisplayName("Should have experiential_requests sensitive fields from V2 migration")
    void shouldHaveExperientialRequestsSensitiveFields() {
        List<SysDictionary> expSensitive =
                sysDictionaryRepository.findByEntityNameAndIsSensitiveTrue("experiential_requests");

        assertThat(expSensitive)
                .extracting(SysDictionary::getAttributeName)
                .contains("budget_ceiling");
    }

    @Test
    @DisplayName("Should have security domain entities registered in sys_db_object")
    void shouldHaveSecurityDomainEntities() {
        Optional<SysDbObject> hyperProfiles = sysDbObjectRepository.findByName("hyper_profiles");
        Optional<SysDbObject> curatedVault = sysDbObjectRepository.findByName("curated_vault");
        Optional<SysDbObject> experientialRequests = sysDbObjectRepository.findByName("experiential_requests");

        assertThat(hyperProfiles).isPresent();
        assertThat(curatedVault).isPresent();
        assertThat(experientialRequests).isPresent();
    }

    @Test
    @DisplayName("Should have V1 foundation entities still present after V2")
    void shouldPreserveV1FoundationEntities() {
        Optional<SysDbObject> sysDbObj = sysDbObjectRepository.findByName("sys_db_object");
        Optional<SysDbObject> sysDict = sysDbObjectRepository.findByName("sys_dictionary");

        assertThat(sysDbObj).isPresent();
        assertThat(sysDict).isPresent();
    }
}
