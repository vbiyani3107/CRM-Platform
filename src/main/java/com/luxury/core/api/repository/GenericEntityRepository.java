package com.luxury.core.api.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * JDBC-based repository for dynamic entity CRUD operations.
 *
 * <p>Unlike JPA repositories, this operates on arbitrary tables registered
 * in {@code sys_db_object}. Table names are validated before use — raw user
 * input is never concatenated into SQL to prevent injection attacks.</p>
 *
 * <p>All queries target the {@code luxury_brand} schema and respect the
 * soft-delete convention ({@code is_deleted = false}).</p>
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class GenericEntityRepository {

    private static final String SCHEMA = "luxury_brand";

    private final JdbcTemplate jdbcTemplate;

    /**
     * Lists all non-deleted rows from the specified entity table with pagination.
     *
     * @param tableName a validated table name from sys_db_object
     * @param pageable  pagination and sort parameters
     * @return a page of row data as maps
     */
    public Page<Map<String, Object>> findAll(String tableName, Pageable pageable) {
        String qualifiedTable = SCHEMA + "." + tableName;

        // Count total non-deleted rows
        String countSql = "SELECT COUNT(*) FROM " + qualifiedTable + " WHERE is_deleted = false";
        Long total = jdbcTemplate.queryForObject(countSql, Long.class);
        if (total == null || total == 0) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        // Fetch paginated rows, ordered by created_at DESC (platform default)
        String selectSql = "SELECT * FROM " + qualifiedTable
                + " WHERE is_deleted = false"
                + " ORDER BY created_at DESC"
                + " LIMIT ? OFFSET ?";

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                selectSql, pageable.getPageSize(), pageable.getOffset());

        return new PageImpl<>(rows, pageable, total);
    }

    /**
     * Finds a single non-deleted row by its UUID primary key.
     *
     * @param tableName a validated table name from sys_db_object
     * @param id        the row UUID
     * @return the row data, or empty if not found or soft-deleted
     */
    public Optional<Map<String, Object>> findById(String tableName, UUID id) {
        String qualifiedTable = SCHEMA + "." + tableName;
        String sql = "SELECT * FROM " + qualifiedTable + " WHERE id = ? AND is_deleted = false";

        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, id);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.getFirst());
    }

    /**
     * Inserts a new row into the specified entity table.
     *
     * <p>Automatically generates a UUID, sets audit timestamps,
     * and initializes version/soft-delete fields.</p>
     *
     * @param tableName  a validated table name from sys_db_object
     * @param attributes the column values to insert
     * @param username   the current user for audit fields
     * @return the full row data including generated id and audit fields
     */
    public Map<String, Object> insert(String tableName, Map<String, Object> attributes, String username) {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();

        // Build the complete row: user attributes + audit fields
        Map<String, Object> fullRow = new LinkedHashMap<>(attributes);
        fullRow.put("id", id);
        fullRow.put("created_at", Timestamp.from(now));
        fullRow.put("updated_at", Timestamp.from(now));
        fullRow.put("created_by", username);
        fullRow.put("updated_by", username);
        fullRow.put("version", 0L);
        fullRow.put("is_deleted", false);

        // Build parameterized INSERT statement
        List<String> columns = new ArrayList<>(fullRow.keySet());
        List<Object> values = new ArrayList<>(fullRow.values());
        String placeholders = String.join(", ", columns.stream().map(c -> "?").toList());
        String columnList = String.join(", ", columns);

        String sql = "INSERT INTO " + SCHEMA + "." + tableName
                + " (" + columnList + ") VALUES (" + placeholders + ")";

        log.debug("Dynamic INSERT into {}.{}: columns={}", SCHEMA, tableName, columns);
        jdbcTemplate.update(sql, values.toArray());

        return findById(tableName, id).orElse(fullRow);
    }

    /**
     * Updates an existing row's attributes. Uses optimistic locking via the version column.
     *
     * @param tableName  a validated table name from sys_db_object
     * @param id         the row UUID to update
     * @param attributes the column values to update (partial update supported)
     * @param username   the current user for the updated_by audit field
     * @return the updated row data
     */
    public Optional<Map<String, Object>> update(String tableName, UUID id,
                                                  Map<String, Object> attributes, String username) {
        String qualifiedTable = SCHEMA + "." + tableName;

        // Build SET clause with parameterized values
        Map<String, Object> updateFields = new LinkedHashMap<>(attributes);
        updateFields.put("updated_at", Timestamp.from(Instant.now()));
        updateFields.put("updated_by", username);

        List<String> setClauses = new ArrayList<>();
        List<Object> params = new ArrayList<>();

        for (Map.Entry<String, Object> entry : updateFields.entrySet()) {
            setClauses.add(entry.getKey() + " = ?");
            params.add(entry.getValue());
        }

        // Also increment version for optimistic locking
        setClauses.add("version = version + 1");

        params.add(id); // WHERE clause parameter

        String sql = "UPDATE " + qualifiedTable
                + " SET " + String.join(", ", setClauses)
                + " WHERE id = ? AND is_deleted = false";

        log.debug("Dynamic UPDATE on {}.{}: id={}, fields={}", SCHEMA, tableName, id, updateFields.keySet());
        int affected = jdbcTemplate.update(sql, params.toArray());

        if (affected == 0) {
            return Optional.empty();
        }

        return findById(tableName, id);
    }

    /**
     * Soft-deletes a row by setting {@code is_deleted = true}.
     *
     * @param tableName a validated table name from sys_db_object
     * @param id        the row UUID to soft-delete
     * @param username  the current user for the updated_by audit field
     * @return true if the row was found and deleted, false otherwise
     */
    public boolean softDelete(String tableName, UUID id, String username) {
        String qualifiedTable = SCHEMA + "." + tableName;
        String sql = "UPDATE " + qualifiedTable
                + " SET is_deleted = true, updated_at = ?, updated_by = ?, version = version + 1"
                + " WHERE id = ? AND is_deleted = false";

        int affected = jdbcTemplate.update(sql, Timestamp.from(Instant.now()), username, id);
        log.debug("Dynamic SOFT-DELETE on {}.{}: id={}, affected={}", SCHEMA, tableName, id, affected);
        return affected > 0;
    }

    /**
     * Checks whether a physical table exists in the luxury_brand schema.
     * Used as a safety check before dynamic SQL operations.
     *
     * @param tableName the table name to check
     * @return true if the table exists
     */
    public boolean tableExists(String tableName) {
        String sql = """
                SELECT COUNT(*) FROM information_schema.tables
                WHERE table_schema = ? AND table_name = ?
                """;
        Long count = jdbcTemplate.queryForObject(sql, Long.class, SCHEMA, tableName);
        return count != null && count > 0;
    }
}
