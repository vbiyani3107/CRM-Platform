package com.luxury.core.persistence.integration;

import com.luxury.core.persistence.model.SysDbObject;
import com.luxury.core.persistence.model.SysDictionary;
import com.luxury.core.persistence.repository.SysDbObjectRepository;
import com.luxury.core.persistence.repository.SysDictionaryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for Spring Data repositories against real PostgreSQL
 * with pgvector. Tests JSONB binding, GIN queries, and full JPA lifecycle.
 */
@SpringBootTest
@Transactional
@DisplayName("Repository Integration Tests (pgvector/pgvector:pg16)")
class RepositoryIT extends AbstractPostgresIT {

    @Autowired
    private SysDbObjectRepository dbObjectRepository;

    @Autowired
    private SysDictionaryRepository dictionaryRepository;

    @Test
    @DisplayName("Should persist and retrieve SysDbObject with JSONB custom_attributes")
    void shouldPersistSysDbObjectWithJsonb() {
        SysDbObject entity = new SysDbObject();
        entity.setName("test_integration_entity");
        entity.setLabel("Integration Test Entity");
        entity.setDescription("Created by integration test");
        entity.setCustomAttributes(Map.of(
                "module", "foundation",
                "version", "1.0",
                "tags", List.of("core", "metadata")
        ));

        SysDbObject saved = dbObjectRepository.saveAndFlush(entity);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();

        Optional<SysDbObject> retrieved = dbObjectRepository.findByName("test_integration_entity");
        assertThat(retrieved).isPresent();

        Map<String, Object> attrs = retrieved.get().getCustomAttributes();
        assertThat(attrs).containsEntry("module", "foundation");
        assertThat(attrs.get("tags")).isInstanceOf(List.class);
    }

    @Test
    @DisplayName("Should persist SysDictionary with validation_rules JSONB")
    void shouldPersistDictionaryWithValidationRules() {
        SysDictionary entry = new SysDictionary();
        entry.setEntityName("hyper_profiles");
        entry.setAttributeName("influence_score_it");
        entry.setDataType("decimal");
        entry.setIsSensitive(false);
        entry.setIsCustom(false);
        entry.setValidationRules(Map.of(
                "min", 0,
                "max", 100,
                "precision", 2
        ));

        SysDictionary saved = dictionaryRepository.saveAndFlush(entry);
        assertThat(saved.getId()).isNotNull();

        List<SysDictionary> results = dictionaryRepository.findByEntityName("hyper_profiles");
        assertThat(results).anyMatch(d ->
                d.getAttributeName().equals("influence_score_it")
                        && d.getValidationRules().containsKey("min")
        );
    }

    @Test
    @DisplayName("Should filter dictionary by JSONB containment query")
    void shouldFilterByJsonbContainment() {
        SysDictionary entry1 = new SysDictionary();
        entry1.setEntityName("curated_vault");
        entry1.setAttributeName("piece_name_it");
        entry1.setDataType("string");
        entry1.setValidationRules(Map.of("searchable", true));

        SysDictionary entry2 = new SysDictionary();
        entry2.setEntityName("curated_vault");
        entry2.setAttributeName("material_comp_it");
        entry2.setDataType("jsonb");
        entry2.setValidationRules(Map.of("searchable", false));

        dictionaryRepository.saveAllAndFlush(List.of(entry1, entry2));

        List<SysDictionary> searchable = dictionaryRepository
                .findByEntityNameAndValidationRulesContaining(
                        "curated_vault",
                        "{\"searchable\": true}"
                );

        assertThat(searchable).hasSize(1);
        assertThat(searchable.get(0).getAttributeName()).isEqualTo("piece_name_it");
    }

    @Test
    @DisplayName("Should respect soft-delete flag via BaseEntity")
    void shouldRespectSoftDelete() {
        SysDbObject entity = new SysDbObject();
        entity.setName("soft_delete_test");
        entity.setLabel("Soft Delete Test");
        entity.setIsDeleted(false);

        SysDbObject saved = dbObjectRepository.saveAndFlush(entity);
        assertThat(saved.getIsDeleted()).isFalse();

        saved.setIsDeleted(true);
        dbObjectRepository.saveAndFlush(saved);

        Optional<SysDbObject> retrieved = dbObjectRepository.findByName("soft_delete_test");
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getIsDeleted()).isTrue();
    }

    @Test
    @DisplayName("Should handle optimistic locking via @Version")
    void shouldTrackVersion() {
        SysDbObject entity = new SysDbObject();
        entity.setName("version_test");
        entity.setLabel("Version Test");

        SysDbObject saved = dbObjectRepository.saveAndFlush(entity);
        Long initialVersion = saved.getVersion();
        assertThat(initialVersion).isNotNull();

        saved.setLabel("Updated Label");
        SysDbObject updated = dbObjectRepository.saveAndFlush(saved);

        assertThat(updated.getVersion()).isGreaterThan(initialVersion);
    }
}
