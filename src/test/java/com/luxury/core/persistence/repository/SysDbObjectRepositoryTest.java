package com.luxury.core.persistence.repository;

import com.luxury.core.persistence.model.SysDbObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for SysDbObjectRepository.
 * Uses @DataJpaTest with H2 in PostgreSQL mode.
 */
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(statements = "CREATE SCHEMA IF NOT EXISTS luxury_brand", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@DisplayName("SysDbObjectRepository Unit Tests")
class SysDbObjectRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private SysDbObjectRepository repository;

    @Test
    @DisplayName("Should save and retrieve entity by name")
    void shouldSaveAndFindByName() {
        // given
        SysDbObject entity = new SysDbObject();
        entity.setName("hyper_profiles");
        entity.setLabel("Hyper Profiles");
        entity.setDescription("HNWI profile management");
        entityManager.persistAndFlush(entity);

        // when
        Optional<SysDbObject> found = repository.findByName("hyper_profiles");

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getLabel()).isEqualTo("Hyper Profiles");
        assertThat(found.get().getId()).isNotNull();
        assertThat(found.get().getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should return empty optional for non-existent entity")
    void shouldReturnEmptyForNonExistent() {
        Optional<SysDbObject> found = repository.findByName("does_not_exist");
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should check existence by name")
    void shouldCheckExistence() {
        SysDbObject entity = new SysDbObject();
        entity.setName("curated_vault");
        entity.setLabel("The Vault");
        entityManager.persistAndFlush(entity);

        assertThat(repository.existsByName("curated_vault")).isTrue();
        assertThat(repository.existsByName("missing")).isFalse();
    }

    @Test
    @DisplayName("Should persist JSONB custom attributes")
    void shouldPersistJsonbAttributes() {
        SysDbObject entity = new SysDbObject();
        entity.setName("test_entity");
        entity.setLabel("Test");
        entity.setCustomAttributes(Map.of(
                "category", "foundation",
                "priority", 1
        ));
        entityManager.persistAndFlush(entity);

        Optional<SysDbObject> found = repository.findByName("test_entity");
        assertThat(found).isPresent();
        assertThat(found.get().getCustomAttributes())
                .containsEntry("category", "foundation");
    }

    @Test
    @DisplayName("Should set default values via BaseEntity lifecycle")
    void shouldSetBaseEntityDefaults() {
        SysDbObject entity = new SysDbObject();
        entity.setName("lifecycle_test");
        entity.setLabel("Lifecycle");
        SysDbObject saved = entityManager.persistAndFlush(entity);

        assertThat(saved.getIsDeleted()).isFalse();
        assertThat(saved.getIsExtensible()).isTrue();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
        assertThat(saved.getVersion()).isNotNull();
    }
}
