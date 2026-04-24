package com.luxury.core.api.controller;

import com.luxury.common.dto.ApiResponse;
import com.luxury.core.api.dto.GenericEntityRequest;
import com.luxury.core.api.dto.GenericEntityResponse;
import com.luxury.core.api.service.GenericEntityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Dynamic CRUD controller for the Bespoke Luxury Platform.
 *
 * <p>Exposes RESTful endpoints at {@code /api/v1/entities/{entityName}} that
 * operate on any entity registered in {@code sys_db_object}. Payloads are
 * validated at runtime against {@code sys_dictionary} field definitions.</p>
 *
 * <p>All responses are wrapped in {@link ApiResponse} and errors follow
 * RFC 7807 Problem Details via the global exception handler.</p>
 *
 * <h3>Endpoints:</h3>
 * <ul>
 *   <li>{@code GET    /entities/{entityName}}       — List with pagination</li>
 *   <li>{@code GET    /entities/{entityName}/{id}}   — Get single record</li>
 *   <li>{@code POST   /entities/{entityName}}       — Create with validation</li>
 *   <li>{@code PUT    /entities/{entityName}/{id}}   — Update with validation</li>
 *   <li>{@code DELETE /entities/{entityName}/{id}}   — Soft-delete</li>
 * </ul>
 */
@Slf4j
@RestController
@RequestMapping("/v1/entities")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class GenericEntityController {

    private static final int DEFAULT_PAGE_SIZE = 20;

    private final GenericEntityService genericEntityService;

    /**
     * Lists all records for the specified entity with pagination.
     *
     * @param entityName the registered entity name (e.g., "hyper_profiles")
     * @param page       zero-based page index (default 0)
     * @param size       page size (default 20)
     * @return paginated list of entity records wrapped in ApiResponse
     */
    @GetMapping("/{entityName}")
    public ResponseEntity<ApiResponse<Page<GenericEntityResponse>>> list(
            @PathVariable String entityName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.debug("GET /entities/{} — page={}, size={}", entityName, page, size);
        Pageable pageable = PageRequest.of(page, Math.min(size, 100));
        Page<GenericEntityResponse> result = genericEntityService.list(entityName, pageable);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    /**
     * Retrieves a single record by its UUID.
     *
     * @param entityName the registered entity name
     * @param id         the record UUID
     * @return the entity record wrapped in ApiResponse
     */
    @GetMapping("/{entityName}/{id}")
    public ResponseEntity<ApiResponse<GenericEntityResponse>> getById(
            @PathVariable String entityName,
            @PathVariable UUID id) {

        log.debug("GET /entities/{}/{}", entityName, id);
        GenericEntityResponse response = genericEntityService.getById(entityName, id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * Creates a new record for the specified entity.
     *
     * @param entityName the registered entity name
     * @param request    the request body containing attributes
     * @return the created entity record wrapped in ApiResponse
     */
    @PostMapping("/{entityName}")
    public ResponseEntity<ApiResponse<GenericEntityResponse>> create(
            @PathVariable String entityName,
            @RequestBody @Valid GenericEntityRequest request) {

        log.debug("POST /entities/{} — attributes={}", entityName, request.getAttributes().keySet());
        GenericEntityResponse response = genericEntityService.create(entityName, request.getAttributes());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response));
    }

    /**
     * Updates an existing record (partial update supported).
     *
     * @param entityName the registered entity name
     * @param id         the record UUID to update
     * @param request    the request body containing attributes to update
     * @return the updated entity record wrapped in ApiResponse
     */
    @PutMapping("/{entityName}/{id}")
    public ResponseEntity<ApiResponse<GenericEntityResponse>> update(
            @PathVariable String entityName,
            @PathVariable UUID id,
            @RequestBody @Valid GenericEntityRequest request) {

        log.debug("PUT /entities/{}/{} — attributes={}", entityName, id, request.getAttributes().keySet());
        GenericEntityResponse response = genericEntityService.update(entityName, id, request.getAttributes());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * Soft-deletes a record.
     *
     * @param entityName the registered entity name
     * @param id         the record UUID to delete
     * @return 204 No Content on success
     */
    @DeleteMapping("/{entityName}/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable String entityName,
            @PathVariable UUID id) {

        log.debug("DELETE /entities/{}/{}", entityName, id);
        genericEntityService.delete(entityName, id);
        return ResponseEntity.noContent().build();
    }
}
