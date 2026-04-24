package com.luxury.core.api.service;

import com.luxury.core.api.dto.GenericEntityResponse;
import com.luxury.core.api.exception.DynamicValidationException;
import com.luxury.core.api.repository.GenericEntityRepository;
import com.luxury.core.api.validation.DynamicPayloadValidator;
import com.luxury.core.persistence.repository.SysDbObjectRepository;
import com.luxury.core.security.service.SecurityContextService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service layer for dynamic entity CRUD operations.
 *
 * <p>Orchestrates the Controller → Service → Repository flow for the
 * generic entity API. Responsibilities:</p>
 * <ul>
 *   <li>Validates that the target entity is registered in {@code sys_db_object}</li>
 *   <li>Validates the physical table exists in the database</li>
 *   <li>Delegates payload validation to {@link DynamicPayloadValidator}</li>
 *   <li>Sets audit fields from the current security context</li>
 *   <li>Delegates persistence to {@link GenericEntityRepository}</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GenericEntityService {

    private final SysDbObjectRepository sysDbObjectRepository;
    private final GenericEntityRepository genericEntityRepository;
    private final DynamicPayloadValidator payloadValidator;
    private final SecurityContextService securityContextService;

    /**
     * Lists all records for the given entity with pagination.
     *
     * @param entityName the registered entity name
     * @param pageable   pagination parameters
     * @return a page of entity response DTOs
     */
    @Transactional(readOnly = true)
    public Page<GenericEntityResponse> list(String entityName, Pageable pageable) {
        validateEntityExists(entityName);

        Page<Map<String, Object>> rows = genericEntityRepository.findAll(entityName, pageable);
        return rows.map(row -> toResponse(entityName, row));
    }

    /**
     * Retrieves a single record by entity name and UUID.
     *
     * @param entityName the registered entity name
     * @param id         the record UUID
     * @return the entity response DTO
     * @throws EntityNotFoundException if the record does not exist
     */
    @Transactional(readOnly = true)
    public GenericEntityResponse getById(String entityName, UUID id) {
        validateEntityExists(entityName);

        Map<String, Object> row = genericEntityRepository.findById(entityName, id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Record with id '" + id + "' not found in entity '" + entityName + "'"));

        return toResponse(entityName, row);
    }

    /**
     * Creates a new record for the given entity.
     *
     * @param entityName the registered entity name
     * @param attributes the payload attributes
     * @return the created entity response DTO
     * @throws DynamicValidationException if payload validation fails
     */
    @Transactional
    public GenericEntityResponse create(String entityName, Map<String, Object> attributes) {
        validateEntityExists(entityName);

        // Validate payload against sys_dictionary
        List<String> violations = payloadValidator.validate(entityName, attributes, false);
        if (!violations.isEmpty()) {
            throw new DynamicValidationException(violations);
        }

        String username = securityContextService.getCurrentUsername();
        log.info("Creating new record in '{}' by user '{}'", entityName, username);

        Map<String, Object> row = genericEntityRepository.insert(entityName, attributes, username);
        return toResponse(entityName, row);
    }

    /**
     * Updates an existing record (partial update supported).
     *
     * @param entityName the registered entity name
     * @param id         the record UUID to update
     * @param attributes the payload attributes to update
     * @return the updated entity response DTO
     * @throws EntityNotFoundException    if the record does not exist
     * @throws DynamicValidationException if payload validation fails
     */
    @Transactional
    public GenericEntityResponse update(String entityName, UUID id, Map<String, Object> attributes) {
        validateEntityExists(entityName);

        // Validate payload (partial update mode — required checks relaxed)
        List<String> violations = payloadValidator.validate(entityName, attributes, true);
        if (!violations.isEmpty()) {
            throw new DynamicValidationException(violations);
        }

        String username = securityContextService.getCurrentUsername();
        log.info("Updating record '{}' in '{}' by user '{}'", id, entityName, username);

        Map<String, Object> row = genericEntityRepository.update(entityName, id, attributes, username)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Record with id '" + id + "' not found in entity '" + entityName + "'"));

        return toResponse(entityName, row);
    }

    /**
     * Soft-deletes a record.
     *
     * @param entityName the registered entity name
     * @param id         the record UUID to delete
     * @throws EntityNotFoundException if the record does not exist
     */
    @Transactional
    public void delete(String entityName, UUID id) {
        validateEntityExists(entityName);

        String username = securityContextService.getCurrentUsername();
        log.info("Soft-deleting record '{}' in '{}' by user '{}'", id, entityName, username);

        boolean deleted = genericEntityRepository.softDelete(entityName, id, username);
        if (!deleted) {
            throw new EntityNotFoundException(
                    "Record with id '" + id + "' not found in entity '" + entityName + "'");
        }
    }

    /**
     * Validates that the entity name is registered in sys_db_object
     * and the corresponding physical table exists.
     *
     * @throws EntityNotFoundException if the entity is not registered
     * @throws IllegalStateException   if the physical table is missing
     */
    private void validateEntityExists(String entityName) {
        if (!sysDbObjectRepository.existsByName(entityName)) {
            throw new EntityNotFoundException(
                    "Entity '" + entityName + "' is not registered in the platform");
        }

        if (!genericEntityRepository.tableExists(entityName)) {
            throw new IllegalStateException(
                    "Entity '" + entityName + "' is registered but its physical table does not exist. "
                            + "A migration may be pending.");
        }
    }

    /**
     * Converts a raw database row (Map) into a {@link GenericEntityResponse} DTO.
     * Separates audit/system fields from business attributes.
     */
    private GenericEntityResponse toResponse(String entityName, Map<String, Object> row) {
        Map<String, Object> attributes = new HashMap<>(row);

        // Extract and remove system/audit fields
        UUID id = extractUuid(attributes.remove("id"));
        Instant createdAt = extractInstant(attributes.remove("created_at"));
        Instant updatedAt = extractInstant(attributes.remove("updated_at"));
        Object createdByVal = attributes.remove("created_by");
        Object updatedByVal = attributes.remove("updated_by");
        String createdBy = createdByVal != null ? createdByVal.toString() : null;
        String updatedBy = updatedByVal != null ? updatedByVal.toString() : null;

        // Remove internal fields that should not be exposed
        attributes.remove("version");
        attributes.remove("is_deleted");

        return GenericEntityResponse.builder()
                .id(id)
                .entityName(entityName)
                .attributes(attributes)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .createdBy(createdBy)
                .updatedBy(updatedBy)
                .build();
    }

    private UUID extractUuid(Object value) {
        if (value instanceof UUID uuid) return uuid;
        if (value instanceof String str) return UUID.fromString(str);
        return null;
    }

    private Instant extractInstant(Object value) {
        if (value instanceof Instant instant) return instant;
        if (value instanceof java.sql.Timestamp ts) return ts.toInstant();
        if (value instanceof java.time.LocalDateTime ldt) return ldt.atZone(java.time.ZoneOffset.UTC).toInstant();
        return null;
    }

    private String extractString(Map<String, Object> row, String key) {
        Object val = row.get(key);
        return val != null ? val.toString() : null;
    }
}
