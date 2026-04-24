package com.luxury.core.persistence.repository;

import com.luxury.core.persistence.model.SysDictionary;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for SysDictionaryRepository.
 * Uses @DataJpaTest with H2 in PostgreSQL mode.
 */
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(statements = "CREATE SCHEMA IF NOT EXISTS luxury_brand", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@DisplayName("SysDictionaryRepository Unit Tests")
class SysDictionaryRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private SysDictionaryRepository repository;

    private SysDictionary createDictEntry(String entityName, String attrName,
                                           String dataType, boolean sensitive, boolean custom) {
        SysDictionary entry = new SysDictionary();
        entry.setEntityName(entityName);
        entry.setAttributeName(attrName);
        entry.setDataType(dataType);
        entry.setIsSensitive(sensitive);
        entry.setIsCustom(custom);
        return entry;
    }

    @Test
    @DisplayName("Should find all attributes for a given entity")
    void shouldFindByEntityName() {
        entityManager.persistAndFlush(
                createDictEntry("hyper_profiles", "full_name", "string", false, false));
        entityManager.persistAndFlush(
                createDictEntry("hyper_profiles", "taste_vector", "vector", false, false));
        entityManager.persistAndFlush(
                createDictEntry("curated_vault", "piece_name", "string", false, false));

        List<SysDictionary> result = repository.findByEntityName("hyper_profiles");

        assertThat(result).hasSize(2);
        assertThat(result).extracting(SysDictionary::getAttributeName)
                .containsExactlyInAnyOrder("full_name", "taste_vector");
    }

    @Test
    @DisplayName("Should find only sensitive attributes")
    void shouldFindSensitiveAttributes() {
        entityManager.persistAndFlush(
                createDictEntry("hyper_profiles", "full_name", "string", false, false));
        entityManager.persistAndFlush(
                createDictEntry("hyper_profiles", "wealth_band", "string", true, false));
        entityManager.persistAndFlush(
                createDictEntry("hyper_profiles", "offshore_entities", "jsonb", true, false));

        List<SysDictionary> result =
                repository.findByEntityNameAndIsSensitiveTrue("hyper_profiles");

        assertThat(result).hasSize(2);
        assertThat(result).extracting(SysDictionary::getAttributeName)
                .containsExactlyInAnyOrder("wealth_band", "offshore_entities");
    }

    @Test
    @DisplayName("Should find only custom (dynamically added) attributes")
    void shouldFindCustomAttributes() {
        entityManager.persistAndFlush(
                createDictEntry("curated_vault", "piece_name", "string", false, false));
        entityManager.persistAndFlush(
                createDictEntry("curated_vault", "dial_color", "string", false, true));

        List<SysDictionary> result =
                repository.findByEntityNameAndIsCustomTrue("curated_vault");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAttributeName()).isEqualTo("dial_color");
    }

    @Test
    @DisplayName("Should check attribute existence on entity")
    void shouldCheckAttributeExists() {
        entityManager.persistAndFlush(
                createDictEntry("hyper_profiles", "vip_tier", "string", false, false));

        assertThat(repository.existsByEntityNameAndAttributeName(
                "hyper_profiles", "vip_tier")).isTrue();
        assertThat(repository.existsByEntityNameAndAttributeName(
                "hyper_profiles", "nonexistent")).isFalse();
    }

    @Test
    @DisplayName("Should persist validation rules as JSONB")
    void shouldPersistValidationRules() {
        SysDictionary entry = createDictEntry("hyper_profiles", "influence_score", "decimal", false, false);
        entry.setValidationRules(Map.of(
                "min", 0,
                "max", 100,
                "precision", 2
        ));
        entityManager.persistAndFlush(entry);

        List<SysDictionary> result = repository.findByEntityName("hyper_profiles");
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getValidationRules())
                .containsEntry("min", 0)
                .containsEntry("max", 100);
    }
}
