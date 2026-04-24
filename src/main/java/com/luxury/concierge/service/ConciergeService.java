package com.luxury.concierge.service;

import com.luxury.concierge.dto.ConciergeAlertResponse;
import com.luxury.concierge.dto.ExperientialRequestDto;
import com.luxury.concierge.dto.ExperientialRequestResponse;
import com.luxury.concierge.event.ConciergeRequestEvent;
import com.luxury.concierge.model.ConciergeAlert;
import com.luxury.concierge.model.ExperientialRequest;
import com.luxury.concierge.repository.ConciergeAlertRepository;
import com.luxury.concierge.repository.ExperientialRequestRepository;
import com.luxury.core.security.service.SecurityContextService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

/**
 * Core service for Concierge & Experiential Management operations.
 *
 * <p>Handles CRUD for experiential requests, state management (fulfill/cancel),
 * alert queries, and fires ambient intelligence events for async LangChain4j
 * processing.</p>
 *
 * <p>Follows the Controller → Service → Repository pattern. All write operations
 * fire {@link ConciergeRequestEvent} events for ambient processing.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConciergeService {

    private final ExperientialRequestRepository requestRepository;
    private final ConciergeAlertRepository alertRepository;
    private final SecurityContextService securityContextService;
    private final ApplicationEventPublisher eventPublisher;

    // ═══════════════════════════════════════════════════════════════════════════
    //  Experiential Requests — CRUD
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Lists all active experiential requests with pagination.
     */
    @Transactional(readOnly = true)
    public Page<ExperientialRequestResponse> listRequests(Pageable pageable) {
        return requestRepository.findAllActive(pageable)
                .map(this::toResponse);
    }

    /**
     * Retrieves a single experiential request by ID.
     *
     * @throws EntityNotFoundException if the request does not exist or is soft-deleted
     */
    @Transactional(readOnly = true)
    public ExperientialRequestResponse getRequestById(UUID id) {
        ExperientialRequest request = findActiveOrThrow(id);
        return toResponse(request);
    }

    /**
     * Creates a new experiential request.
     *
     * <p>Auto-generates a sequential number (EXP0000001 format),
     * sets audit fields from the current security context, and fires
     * a CREATED event for ambient intelligence processing.</p>
     */
    @Transactional
    public ExperientialRequestResponse createRequest(ExperientialRequestDto dto) {
        String username = securityContextService.getCurrentUsername();
        int nextSeq = requestRepository.getNextSequenceNumber();
        String number = String.format("EXP%07d", nextSeq);

        ExperientialRequest request = ExperientialRequest.builder()
                .number(number)
                .profileId(dto.getProfileId())
                .requestType(dto.getRequestType())
                .discretionLevel(dto.getDiscretionLevel() != null
                        ? dto.getDiscretionLevel()
                        : ExperientialRequest.DiscretionLevel.High)
                .state(ExperientialRequest.RequestState.Open)
                .conciergeNotes(dto.getConciergeNotes())
                .targetDate(dto.getTargetDate())
                .customAttributes(dto.getCustomAttributes() != null
                        ? dto.getCustomAttributes()
                        : Map.of())
                .build();

        request.setCreatedBy(username);
        request.setUpdatedBy(username);

        ExperientialRequest saved = requestRepository.save(request);
        log.info("Created experiential request '{}' for profile '{}' by '{}'",
                saved.getNumber(), saved.getProfileId(), username);

        // Fire ambient event
        eventPublisher.publishEvent(new ConciergeRequestEvent(
                this, saved, ConciergeRequestEvent.EventType.CREATED));

        return toResponse(saved);
    }

    /**
     * Updates an existing experiential request (partial update).
     *
     * @throws EntityNotFoundException if the request does not exist
     */
    @Transactional
    public ExperientialRequestResponse updateRequest(UUID id, ExperientialRequestDto dto) {
        ExperientialRequest request = findActiveOrThrow(id);
        String username = securityContextService.getCurrentUsername();

        if (dto.getRequestType() != null) {
            request.setRequestType(dto.getRequestType());
        }
        if (dto.getDiscretionLevel() != null) {
            request.setDiscretionLevel(dto.getDiscretionLevel());
        }
        if (dto.getConciergeNotes() != null) {
            request.setConciergeNotes(dto.getConciergeNotes());
        }
        if (dto.getTargetDate() != null) {
            request.setTargetDate(dto.getTargetDate());
        }
        if (dto.getCustomAttributes() != null) {
            request.setCustomAttributes(dto.getCustomAttributes());
        }

        request.setUpdatedBy(username);

        ExperientialRequest saved = requestRepository.save(request);
        log.info("Updated experiential request '{}' by '{}'", saved.getNumber(), username);

        eventPublisher.publishEvent(new ConciergeRequestEvent(
                this, saved, ConciergeRequestEvent.EventType.UPDATED));

        return toResponse(saved);
    }

    /**
     * Marks a request as fulfilled.
     *
     * @throws EntityNotFoundException if the request does not exist
     * @throws IllegalStateException   if the request is already fulfilled or cancelled
     */
    @Transactional
    public ExperientialRequestResponse fulfillRequest(UUID id) {
        ExperientialRequest request = findActiveOrThrow(id);

        if (request.getState() == ExperientialRequest.RequestState.Fulfilled) {
            throw new IllegalStateException("Request '" + request.getNumber() + "' is already fulfilled");
        }
        if (request.getState() == ExperientialRequest.RequestState.Cancelled) {
            throw new IllegalStateException("Cannot fulfill a cancelled request: " + request.getNumber());
        }

        String username = securityContextService.getCurrentUsername();
        request.setState(ExperientialRequest.RequestState.Fulfilled);
        request.setUpdatedBy(username);

        ExperientialRequest saved = requestRepository.save(request);
        log.info("Fulfilled experiential request '{}' by '{}'", saved.getNumber(), username);

        eventPublisher.publishEvent(new ConciergeRequestEvent(
                this, saved, ConciergeRequestEvent.EventType.FULFILLED));

        return toResponse(saved);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  Concierge Alerts
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Lists unread alerts for a specific associate.
     */
    @Transactional(readOnly = true)
    public Page<ConciergeAlertResponse> getUnreadAlerts(UUID associateId, Pageable pageable) {
        return alertRepository.findUnreadByAssociateId(associateId, pageable)
                .map(this::toAlertResponse);
    }

    /**
     * Marks an alert as read.
     *
     * @throws EntityNotFoundException if the alert does not exist
     */
    @Transactional
    public void markAlertAsRead(UUID alertId) {
        int updated = alertRepository.markAsRead(alertId);
        if (updated == 0) {
            throw new EntityNotFoundException("Alert with id '" + alertId + "' not found");
        }
        log.debug("Marked alert '{}' as read", alertId);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  Private Helpers
    // ═══════════════════════════════════════════════════════════════════════════

    private ExperientialRequest findActiveOrThrow(UUID id) {
        return requestRepository.findActiveById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Experiential request with id '" + id + "' not found"));
    }

    private ExperientialRequestResponse toResponse(ExperientialRequest entity) {
        return ExperientialRequestResponse.builder()
                .id(entity.getId())
                .number(entity.getNumber())
                .profileId(entity.getProfileId())
                .requestType(entity.getRequestType())
                .discretionLevel(entity.getDiscretionLevel())
                .state(entity.getState())
                .conciergeNotes(entity.getConciergeNotes())
                .targetDate(entity.getTargetDate())
                .customAttributes(entity.getCustomAttributes())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .build();
    }

    private ConciergeAlertResponse toAlertResponse(ConciergeAlert entity) {
        return ConciergeAlertResponse.builder()
                .id(entity.getId())
                .profileId(entity.getProfileId())
                .associateId(entity.getAssociateId())
                .alertType(entity.getAlertType())
                .message(entity.getMessage())
                .suggestedItems(entity.getSuggestedItems())
                .isRead(entity.getIsRead())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
