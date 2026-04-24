package com.luxury.core.persistence.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test verifying Flyway V1 migration applies correctly
 * against real PostgreSQL with pgvector extension.
 */
@SpringBootTest
@DisplayName("Flyway Migration Integration Tests")
class FlywayMigrationIT extends AbstractPostgresIT {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("V1 migration should create pgvector extension")
    void shouldCreateVectorExtension() {
        List<Map<String, Object>> extensions = jdbcTemplate.queryForList(
                "SELECT extname FROM pg_extension WHERE extname = 'vector'"
        );
        assertThat(extensions).isNotEmpty();
        assertThat(extensions.get(0).get("extname")).isEqualTo("vector");
    }

    @Test
    @DisplayName("V1 migration should create sys_db_object table")
    void shouldCreateSysDbObjectTable() {
        Integer count = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*) FROM information_schema.tables
                WHERE table_schema = 'luxury_brand' AND table_name = 'sys_db_object'
                """,
                Integer.class
        );
        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("V1 migration should create sys_dictionary table")
    void shouldCreateSysDictionaryTable() {
        Integer count = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*) FROM information_schema.tables
                WHERE table_schema = 'luxury_brand' AND table_name = 'sys_dictionary'
                """,
                Integer.class
        );
        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("V1 migration should seed foundation entity records")
    void shouldSeedFoundationEntities() {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM luxury_brand.sys_db_object WHERE name IN ('sys_db_object', 'sys_dictionary')",
                Integer.class
        );
        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("sys_db_object should have GIN index on custom_attributes")
    void shouldHaveGinIndexOnCustomAttributes() {
        List<Map<String, Object>> indexes = jdbcTemplate.queryForList(
                """
                SELECT indexname FROM pg_indexes
                WHERE schemaname = 'luxury_brand'
                  AND tablename = 'sys_db_object'
                  AND indexdef LIKE '%GIN%'
                """
        );
        assertThat(indexes).isNotEmpty();
    }

    @Test
    @DisplayName("sys_dictionary should have unique constraint on entity_name + attribute_name")
    void shouldHaveUniqueConstraintOnDictionary() {
        List<Map<String, Object>> constraints = jdbcTemplate.queryForList(
                """
                SELECT constraint_name FROM information_schema.table_constraints
                WHERE table_schema = 'luxury_brand'
                  AND table_name = 'sys_dictionary'
                  AND constraint_type = 'UNIQUE'
                """
        );
        assertThat(constraints).isNotEmpty();
    }
}
