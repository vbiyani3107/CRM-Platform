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
 * Integration tests validating pgvector functionality:
 * - Vector data type availability
 * - HNSW index creation
 * - Cosine similarity / L2 distance queries
 *
 * These tests prove the infrastructure is ready for 1536-dimensional
 * OpenAI embeddings used by the Aesthetic Matching engine.
 */
@SpringBootTest
@DisplayName("Vector Search Integration Tests (pgvector)")
class VectorSearchIT extends AbstractPostgresIT {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("Should support vector data type in DDL")
    void shouldSupportVectorDataType() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS luxury_brand.vector_test (
                    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                    name VARCHAR(100),
                    embedding vector(3)
                )
                """);

        Integer count = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*) FROM information_schema.columns
                WHERE table_schema = 'luxury_brand'
                  AND table_name = 'vector_test'
                  AND column_name = 'embedding'
                """,
                Integer.class
        );
        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("Should create HNSW index on vector column")
    void shouldCreateHnswIndex() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS luxury_brand.hnsw_test (
                    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                    embedding vector(3)
                )
                """);

        jdbcTemplate.execute("""
                CREATE INDEX IF NOT EXISTS idx_hnsw_test_embedding
                ON luxury_brand.hnsw_test USING hnsw (embedding vector_l2_ops)
                """);

        List<Map<String, Object>> indexes = jdbcTemplate.queryForList(
                """
                SELECT indexname FROM pg_indexes
                WHERE schemaname = 'luxury_brand'
                  AND tablename = 'hnsw_test'
                  AND indexdef LIKE '%hnsw%'
                """
        );
        assertThat(indexes).isNotEmpty();
    }

    @Test
    @DisplayName("Should perform L2 distance nearest-neighbor query")
    void shouldPerformL2DistanceQuery() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS luxury_brand.l2_test (
                    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                    name VARCHAR(100),
                    embedding vector(3)
                )
                """);

        // Insert test vectors
        jdbcTemplate.update(
                "INSERT INTO luxury_brand.l2_test (name, embedding) VALUES (?, ?::vector)",
                "minimalist", "[0.1, 0.2, 0.3]"
        );
        jdbcTemplate.update(
                "INSERT INTO luxury_brand.l2_test (name, embedding) VALUES (?, ?::vector)",
                "avant_garde", "[0.9, 0.8, 0.7]"
        );
        jdbcTemplate.update(
                "INSERT INTO luxury_brand.l2_test (name, embedding) VALUES (?, ?::vector)",
                "classic", "[0.15, 0.25, 0.35]"
        );

        // Query: find nearest to [0.1, 0.2, 0.3] (should be 'minimalist')
        List<Map<String, Object>> results = jdbcTemplate.queryForList(
                """
                SELECT name FROM luxury_brand.l2_test
                ORDER BY embedding <-> '[0.1, 0.2, 0.3]'::vector
                LIMIT 2
                """
        );

        assertThat(results).hasSize(2);
        assertThat(results.get(0).get("name")).isEqualTo("minimalist");
        assertThat(results.get(1).get("name")).isEqualTo("classic");
    }

    @Test
    @DisplayName("Should perform cosine similarity query")
    void shouldPerformCosineSimilarityQuery() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS luxury_brand.cosine_test (
                    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                    name VARCHAR(100),
                    embedding vector(3)
                )
                """);

        jdbcTemplate.update(
                "INSERT INTO luxury_brand.cosine_test (name, embedding) VALUES (?, ?::vector)",
                "dark_moody", "[0.1, 0.9, 0.1]"
        );
        jdbcTemplate.update(
                "INSERT INTO luxury_brand.cosine_test (name, embedding) VALUES (?, ?::vector)",
                "bright_playful", "[0.9, 0.1, 0.9]"
        );

        // Query: cosine distance (<=>) — closer to dark_moody
        List<Map<String, Object>> results = jdbcTemplate.queryForList(
                """
                SELECT name, (embedding <=> '[0.1, 0.85, 0.15]'::vector) AS distance
                FROM luxury_brand.cosine_test
                ORDER BY embedding <=> '[0.1, 0.85, 0.15]'::vector
                LIMIT 1
                """
        );

        assertThat(results).hasSize(1);
        assertThat(results.get(0).get("name")).isEqualTo("dark_moody");
    }
}
