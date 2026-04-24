package com.luxury.core.persistence.config;

import com.pgvector.PGvector;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Persistence configuration for pgvector type registration.
 * Registers the PGvector type with the JDBC driver so that vector columns
 * can be read/written natively.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class PersistenceConfig {

    private final DataSource dataSource;

    /**
     * Register pgvector JDBC type on application startup.
     * This ensures the PostgreSQL driver recognizes the 'vector' data type.
     */
    @PostConstruct
    public void registerPgVectorType() {
        try (Connection conn = dataSource.getConnection()) {
            PGvector.addVectorType(conn);
            log.info("pgvector JDBC type registered successfully");
        } catch (SQLException e) {
            log.warn("Could not register pgvector type (expected in test/H2 environments): {}", e.getMessage());
        }
    }
}

