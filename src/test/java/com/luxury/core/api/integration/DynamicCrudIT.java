package com.luxury.core.api.integration;

import com.luxury.common.dto.ApiResponse;
import com.luxury.core.api.dto.GenericEntityRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Full integration test exercising the Dynamic CRUD API against a real
 * pgvector-enabled PostgreSQL instance via Testcontainers.
 *
 * <p>Tests the complete request lifecycle: HTTP → Controller → Service →
 * Repository → PostgreSQL, including Flyway migrations (V1–V3).</p>
 *
 * <p>IMPORTANT: Uses {@code pgvector/pgvector:pg16} per Session 3 requirements.</p>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DynamicCrudIT extends AbstractDynamicApiIT {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static UUID createdRecordId;

    /**
     * Creates a physical table for hyper_profiles so the dynamic CRUD can operate.
     * In production this would be done by the application's table provisioning system.
     */
    private void ensureHyperProfilesTableExists() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS luxury_brand.hyper_profiles (
                    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                    first_name  VARCHAR(100),
                    last_name   VARCHAR(100),
                    email       VARCHAR(255),
                    phone       VARCHAR(50),
                    notes       TEXT,
                    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    created_by  VARCHAR(255),
                    updated_by  VARCHAR(255),
                    version     BIGINT DEFAULT 0,
                    is_deleted  BOOLEAN DEFAULT FALSE
                )
                """);
    }

    private HttpHeaders authHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // TestSecurityConfig provides a mock JwtDecoder — use any bearer token
        headers.setBearerAuth("test-jwt-token");
        return headers;
    }

    @Test
    @Order(1)
    @DisplayName("V3 migration seeds dictionary definitions for hyper_profiles")
    void v3MigrationSeedsDictionary() {
        // Flyway should have run V1, V2, V3 on the Testcontainer
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM luxury_brand.sys_dictionary WHERE entity_name = 'hyper_profiles'",
                Long.class
        );
        // V2 seeds 5 sensitive fields + V3 seeds 8 standard fields = 13 total
        assertThat(count).isGreaterThanOrEqualTo(8);
    }

    @Test
    @Order(2)
    @DisplayName("Create a hyper_profiles record via POST")
    void createRecord() {
        ensureHyperProfilesTableExists();

        GenericEntityRequest request = GenericEntityRequest.builder()
                .attributes(Map.of(
                        "first_name", "Arabella",
                        "last_name", "Ashton-Whitley",
                        "email", "arabella@luxury.com"
                ))
                .build();

        ResponseEntity<Map> response = restTemplate.exchange(
                "/v1/entities/hyper_profiles",
                HttpMethod.POST,
                new HttpEntity<>(request, authHeaders()),
                Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Map body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("success")).isEqualTo(true);

        Map data = (Map) body.get("data");
        assertThat(data).containsKey("id");
        createdRecordId = UUID.fromString((String) data.get("id"));
        assertThat(createdRecordId).isNotNull();
    }

    @Test
    @Order(3)
    @DisplayName("Read back the created record via GET")
    void readRecord() {
        assertThat(createdRecordId).isNotNull();

        ResponseEntity<Map> response = restTemplate.exchange(
                "/v1/entities/hyper_profiles/" + createdRecordId,
                HttpMethod.GET,
                new HttpEntity<>(authHeaders()),
                Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map body = response.getBody();
        assertThat(body.get("success")).isEqualTo(true);

        Map data = (Map) body.get("data");
        Map attrs = (Map) data.get("attributes");
        assertThat(attrs.get("first_name")).isEqualTo("Arabella");
        assertThat(attrs.get("last_name")).isEqualTo("Ashton-Whitley");
    }

    @Test
    @Order(4)
    @DisplayName("Update the record via PUT")
    void updateRecord() {
        assertThat(createdRecordId).isNotNull();

        GenericEntityRequest request = GenericEntityRequest.builder()
                .attributes(Map.of("first_name", "Isabella"))
                .build();

        ResponseEntity<Map> response = restTemplate.exchange(
                "/v1/entities/hyper_profiles/" + createdRecordId,
                HttpMethod.PUT,
                new HttpEntity<>(request, authHeaders()),
                Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map data = (Map) response.getBody().get("data");
        Map attrs = (Map) data.get("attributes");
        assertThat(attrs.get("first_name")).isEqualTo("Isabella");
    }

    @Test
    @Order(5)
    @DisplayName("Soft-delete the record via DELETE")
    void deleteRecord() {
        assertThat(createdRecordId).isNotNull();

        ResponseEntity<Void> response = restTemplate.exchange(
                "/v1/entities/hyper_profiles/" + createdRecordId,
                HttpMethod.DELETE,
                new HttpEntity<>(authHeaders()),
                Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    @Order(6)
    @DisplayName("Deleted record returns 404")
    void deletedRecordReturns404() {
        assertThat(createdRecordId).isNotNull();

        ResponseEntity<Map> response = restTemplate.exchange(
                "/v1/entities/hyper_profiles/" + createdRecordId,
                HttpMethod.GET,
                new HttpEntity<>(authHeaders()),
                Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @Order(7)
    @DisplayName("Invalid payload returns 400 with validation errors")
    void invalidPayloadReturns400() {
        ensureHyperProfilesTableExists();

        GenericEntityRequest request = GenericEntityRequest.builder()
                .attributes(Map.of("bogus_field", "value"))
                .build();

        ResponseEntity<Map> response = restTemplate.exchange(
                "/v1/entities/hyper_profiles",
                HttpMethod.POST,
                new HttpEntity<>(request, authHeaders()),
                Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        Map body = response.getBody();
        assertThat(body.get("success")).isEqualTo(false);
        Map error = (Map) body.get("error");
        assertThat(error.get("type")).isEqualTo("https://luxury.com/errors/dynamic-validation");
    }

    @Test
    @Order(8)
    @DisplayName("Unknown entity name returns 404")
    void unknownEntityReturns404() {
        ResponseEntity<Map> response = restTemplate.exchange(
                "/v1/entities/phantom_entity",
                HttpMethod.GET,
                new HttpEntity<>(authHeaders()),
                Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @Order(9)
    @DisplayName("Pagination returns correct page structure")
    void paginationWorks() {
        ensureHyperProfilesTableExists();

        // Create 3 records
        for (int i = 0; i < 3; i++) {
            GenericEntityRequest req = GenericEntityRequest.builder()
                    .attributes(Map.of(
                            "first_name", "Client" + i,
                            "last_name", "Test"
                    ))
                    .build();
            restTemplate.exchange(
                    "/v1/entities/hyper_profiles",
                    HttpMethod.POST,
                    new HttpEntity<>(req, authHeaders()),
                    Map.class
            );
        }

        // Fetch page 0, size 2
        ResponseEntity<Map> response = restTemplate.exchange(
                "/v1/entities/hyper_profiles?page=0&size=2",
                HttpMethod.GET,
                new HttpEntity<>(authHeaders()),
                Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map data = (Map) response.getBody().get("data");
        assertThat((Integer) data.get("size")).isEqualTo(2);
        assertThat((Integer) data.get("total_elements")).isGreaterThanOrEqualTo(3);
    }
}
