package com.luxury.concierge.controller;

import com.luxury.common.dto.ApiResponse;
import com.luxury.concierge.dto.ConciergeAlertResponse;
import com.luxury.concierge.dto.ExperientialRequestDto;
import com.luxury.concierge.dto.ExperientialRequestResponse;
import com.luxury.concierge.service.ConciergeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * REST controller for Concierge & Experiential Management.
 *
 * <p>Exposes endpoints for managing luxury experiential requests and
 * concierge alerts. All responses are wrapped in {@link ApiResponse}
 * and errors follow RFC 7807 Problem Details.</p>
 *
 * <h3>Endpoints (per spec §1.1):</h3>
 * <ul>
 *   <li>{@code GET    /concierge/requests}             — List experiential requests</li>
 *   <li>{@code GET    /concierge/requests/{id}}         — Get request details</li>
 *   <li>{@code POST   /concierge/requests}             — Create experiential request</li>
 *   <li>{@code PATCH  /concierge/requests/{id}}         — Update request</li>
 *   <li>{@code POST   /concierge/requests/{id}/fulfill} — Mark as fulfilled</li>
 *   <li>{@code GET    /concierge/alerts}                — List unread alerts for associate</li>
 *   <li>{@code POST   /concierge/alerts/{id}/read}      — Mark alert as read</li>
 * </ul>
 */
@Slf4j
@RestController
@RequestMapping("/v1/concierge")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class ConciergeController {

    private static final int DEFAULT_PAGE_SIZE = 20;

    private final ConciergeService conciergeService;

    // ═══════════════════════════════════════════════════════════════════════════
    //  Experiential Requests
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Lists all active experiential requests with pagination.
     *
     * @param page zero-based page index (default 0)
     * @param size page size (default 20, max 100)
     * @return paginated list of experiential requests
     */
    @GetMapping("/requests")
    public ResponseEntity<ApiResponse<Page<ExperientialRequestResponse>>> listRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.debug("GET /concierge/requests — page={}, size={}", page, size);
        Pageable pageable = PageRequest.of(page, Math.min(size, 100));
        Page<ExperientialRequestResponse> result = conciergeService.listRequests(pageable);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    /**
     * Retrieves a single experiential request by its UUID.
     *
     * @param id the request UUID
     * @return the experiential request details
     */
    @GetMapping("/requests/{id}")
    public ResponseEntity<ApiResponse<ExperientialRequestResponse>> getRequestById(
            @PathVariable UUID id) {

        log.debug("GET /concierge/requests/{}", id);
        ExperientialRequestResponse response = conciergeService.getRequestById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * Creates a new experiential request.
     *
     * @param dto the request body
     * @return the created request (201 Created)
     */
    @PostMapping("/requests")
    public ResponseEntity<ApiResponse<ExperientialRequestResponse>> createRequest(
            @RequestBody @Valid ExperientialRequestDto dto) {

        log.debug("POST /concierge/requests — type='{}', profile='{}'",
                dto.getRequestType(), dto.getProfileId());
        ExperientialRequestResponse response = conciergeService.createRequest(dto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response));
    }

    /**
     * Updates an existing experiential request (partial update).
     *
     * @param id  the request UUID to update
     * @param dto the fields to update
     * @return the updated request
     */
    @PatchMapping("/requests/{id}")
    public ResponseEntity<ApiResponse<ExperientialRequestResponse>> updateRequest(
            @PathVariable UUID id,
            @RequestBody ExperientialRequestDto dto) {

        log.debug("PATCH /concierge/requests/{}", id);
        ExperientialRequestResponse response = conciergeService.updateRequest(id, dto);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * Marks an experiential request as fulfilled.
     *
     * @param id the request UUID to fulfill
     * @return the fulfilled request
     */
    @PostMapping("/requests/{id}/fulfill")
    public ResponseEntity<ApiResponse<ExperientialRequestResponse>> fulfillRequest(
            @PathVariable UUID id) {

        log.debug("POST /concierge/requests/{}/fulfill", id);
        ExperientialRequestResponse response = conciergeService.fulfillRequest(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  Concierge Alerts
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Lists unread concierge alerts for a specific associate.
     *
     * @param associateId the associate UUID
     * @param page        zero-based page index (default 0)
     * @param size        page size (default 20, max 100)
     * @return paginated list of unread alerts
     */
    @GetMapping("/alerts")
    public ResponseEntity<ApiResponse<Page<ConciergeAlertResponse>>> getAlerts(
            @RequestParam UUID associateId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.debug("GET /concierge/alerts — associate={}, page={}, size={}", associateId, page, size);
        Pageable pageable = PageRequest.of(page, Math.min(size, 100));
        Page<ConciergeAlertResponse> result = conciergeService.getUnreadAlerts(associateId, pageable);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    /**
     * Marks a concierge alert as read.
     *
     * @param id the alert UUID
     * @return 204 No Content on success
     */
    @PostMapping("/alerts/{id}/read")
    public ResponseEntity<Void> markAlertAsRead(@PathVariable UUID id) {

        log.debug("POST /concierge/alerts/{}/read", id);
        conciergeService.markAlertAsRead(id);
        return ResponseEntity.noContent().build();
    }
}
