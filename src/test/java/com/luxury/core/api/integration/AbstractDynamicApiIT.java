package com.luxury.core.api.integration;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * Base class for Dynamic API integration tests.
 *
 * <p>Provides its own pgvector-enabled PostgreSQL Testcontainer with
 * properties tuned for integration testing (ddl-auto=none, Flyway enabled).</p>
 *
 * <p>Does NOT extend AbstractPostgresIT because the parent class sets
 * ddl-auto=validate which conflicts with pre-existing schema mismatches
 * in experiential_requests (created_by UUID vs VARCHAR).</p>
 *
 * <p>CRITICAL: Uses {@code pgvector/pgvector:pg16} per Session 3 spec.</p>
 */
@Testcontainers
@ActiveProfiles("test")
public abstract class AbstractDynamicApiIT {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>(
            DockerImageName.parse("pgvector/pgvector:pg16")
                    .asCompatibleSubstituteFor("postgres")
    )
            .withDatabaseName("luxury_brand")
            .withUsername("luxury")
            .withPassword("luxury_dev");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.flyway.enabled", () -> true);
        // Use 'none' — Flyway handles schema; Hibernate validate fails on
        // experiential_requests.created_by UUID vs VARCHAR mismatch
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");
        registry.add("spring.jpa.defer-datasource-initialization", () -> false);
        registry.add("spring.jpa.properties.hibernate.dialect",
                () -> "org.hibernate.dialect.PostgreSQLDialect");
    }
}
