package com.luxury.concierge.service;

import com.luxury.concierge.dto.ExperientialRequestDto;
import com.luxury.concierge.dto.ExperientialRequestResponse;
import com.luxury.concierge.event.ConciergeRequestEvent;
import com.luxury.concierge.model.ConciergeAlert;
import com.luxury.concierge.model.ExperientialRequest;
import com.luxury.concierge.repository.ConciergeAlertRepository;
import com.luxury.concierge.repository.ExperientialRequestRepository;
import com.luxury.core.security.service.SecurityContextService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ConciergeService}.
 *
 * <p>Uses @Mock for all dependencies. Verifies that ambient events are
 * fired using Spring's ApplicationEventPublisher.</p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ConciergeService Unit Tests")
class ConciergeServiceTest {

    @Mock
    private ExperientialRequestRepository requestRepository;

    @Mock
    private ConciergeAlertRepository alertRepository;

    @Mock
    private SecurityContextService securityContextService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ConciergeService conciergeService;

    @Captor
    private ArgumentCaptor<ConciergeRequestEvent> eventCaptor;

    private UUID testProfileId;
    private UUID testRequestId;
    private ExperientialRequest testRequest;

    @BeforeEach
    void setUp() {
        testProfileId = UUID.randomUUID();
        testRequestId = UUID.randomUUID();

        testRequest = ExperientialRequest.builder()
                .number("EXP0000001")
                .profileId(testProfileId)
                .requestType("Yacht Charter")
                .discretionLevel(ExperientialRequest.DiscretionLevel.High)
                .state(ExperientialRequest.RequestState.Open)
                .conciergeNotes("Client prefers Mediterranean routes")
                .targetDate(LocalDate.now().plusDays(30))
                .customAttributes(Map.of("yacht_size", "50m+"))
                .build();
        testRequest.setId(testRequestId);
        testRequest.setCreatedAt(Instant.now());
        testRequest.setUpdatedAt(Instant.now());
        testRequest.setCreatedBy("associate@luxury.com");
        testRequest.setUpdatedBy("associate@luxury.com");
        testRequest.setIsDeleted(false);
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  List Requests
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("List Requests")
    class ListRequestsTests {

        @Test
        @DisplayName("Should return paginated active requests")
        void shouldReturnPaginatedActiveRequests() {
            Pageable pageable = PageRequest.of(0, 20);
            Page<ExperientialRequest> page = new PageImpl<>(List.of(testRequest), pageable, 1);
            when(requestRepository.findAllActive(pageable)).thenReturn(page);

            Page<ExperientialRequestResponse> result = conciergeService.listRequests(pageable);

            assertEquals(1, result.getTotalElements());
            assertEquals("EXP0000001", result.getContent().get(0).getNumber());
            verify(requestRepository).findAllActive(pageable);
        }

        @Test
        @DisplayName("Should return empty page when no requests exist")
        void shouldReturnEmptyPage() {
            Pageable pageable = PageRequest.of(0, 20);
            when(requestRepository.findAllActive(pageable)).thenReturn(Page.empty());

            Page<ExperientialRequestResponse> result = conciergeService.listRequests(pageable);

            assertEquals(0, result.getTotalElements());
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  Get Request By ID
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Get Request By ID")
    class GetRequestByIdTests {

        @Test
        @DisplayName("Should return request when found")
        void shouldReturnRequestWhenFound() {
            when(requestRepository.findActiveById(testRequestId)).thenReturn(Optional.of(testRequest));

            ExperientialRequestResponse result = conciergeService.getRequestById(testRequestId);

            assertNotNull(result);
            assertEquals("EXP0000001", result.getNumber());
            assertEquals(testProfileId, result.getProfileId());
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException when not found")
        void shouldThrowWhenNotFound() {
            UUID unknownId = UUID.randomUUID();
            when(requestRepository.findActiveById(unknownId)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> conciergeService.getRequestById(unknownId));
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  Create Request
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Create Request")
    class CreateRequestTests {

        @Test
        @DisplayName("Should create request and fire CREATED event")
        void shouldCreateAndFireEvent() {
            when(securityContextService.getCurrentUsername()).thenReturn("associate@luxury.com");
            when(requestRepository.getNextSequenceNumber()).thenReturn(1);
            when(requestRepository.save(any(ExperientialRequest.class))).thenReturn(testRequest);

            ExperientialRequestDto dto = ExperientialRequestDto.builder()
                    .profileId(testProfileId)
                    .requestType("Yacht Charter")
                    .conciergeNotes("Client prefers Mediterranean routes")
                    .targetDate(LocalDate.now().plusDays(30))
                    .build();

            ExperientialRequestResponse result = conciergeService.createRequest(dto);

            assertNotNull(result);
            assertEquals("EXP0000001", result.getNumber());

            // Verify ambient event was published
            verify(eventPublisher).publishEvent(eventCaptor.capture());
            ConciergeRequestEvent event = eventCaptor.getValue();
            assertEquals(ConciergeRequestEvent.EventType.CREATED, event.getEventType());
            assertEquals("EXP0000001", event.getRequest().getNumber());
        }

        @Test
        @DisplayName("Should auto-generate EXP number format")
        void shouldAutoGenerateNumber() {
            when(securityContextService.getCurrentUsername()).thenReturn("associate@luxury.com");
            when(requestRepository.getNextSequenceNumber()).thenReturn(42);
            when(requestRepository.save(any(ExperientialRequest.class))).thenAnswer(inv -> {
                ExperientialRequest saved = inv.getArgument(0);
                saved.setId(UUID.randomUUID());
                return saved;
            });

            ExperientialRequestDto dto = ExperientialRequestDto.builder()
                    .profileId(testProfileId)
                    .requestType("Private Viewing")
                    .build();

            ExperientialRequestResponse result = conciergeService.createRequest(dto);
            assertEquals("EXP0000042", result.getNumber());
        }

        @Test
        @DisplayName("Should default discretion level to High when not specified")
        void shouldDefaultDiscretionLevel() {
            when(securityContextService.getCurrentUsername()).thenReturn("associate@luxury.com");
            when(requestRepository.getNextSequenceNumber()).thenReturn(1);

            ArgumentCaptor<ExperientialRequest> captor = ArgumentCaptor.forClass(ExperientialRequest.class);
            when(requestRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

            ExperientialRequestDto dto = ExperientialRequestDto.builder()
                    .profileId(testProfileId)
                    .requestType("Spa/Repair")
                    .build();

            conciergeService.createRequest(dto);

            assertEquals(ExperientialRequest.DiscretionLevel.High,
                    captor.getValue().getDiscretionLevel());
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  Update Request
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Update Request")
    class UpdateRequestTests {

        @Test
        @DisplayName("Should update partial fields and fire UPDATED event")
        void shouldUpdatePartialFields() {
            when(requestRepository.findActiveById(testRequestId)).thenReturn(Optional.of(testRequest));
            when(securityContextService.getCurrentUsername()).thenReturn("director@luxury.com");
            when(requestRepository.save(any(ExperientialRequest.class))).thenReturn(testRequest);

            ExperientialRequestDto dto = ExperientialRequestDto.builder()
                    .conciergeNotes("Updated: include sunset cruise")
                    .build();

            conciergeService.updateRequest(testRequestId, dto);

            verify(eventPublisher).publishEvent(eventCaptor.capture());
            assertEquals(ConciergeRequestEvent.EventType.UPDATED, eventCaptor.getValue().getEventType());
        }

        @Test
        @DisplayName("Should throw when updating non-existent request")
        void shouldThrowWhenUpdatingNonExistent() {
            UUID unknownId = UUID.randomUUID();
            when(requestRepository.findActiveById(unknownId)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> conciergeService.updateRequest(unknownId, new ExperientialRequestDto()));
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  Fulfill Request
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Fulfill Request")
    class FulfillRequestTests {

        @Test
        @DisplayName("Should fulfill open request and fire FULFILLED event")
        void shouldFulfillOpenRequest() {
            testRequest.setState(ExperientialRequest.RequestState.Open);
            when(requestRepository.findActiveById(testRequestId)).thenReturn(Optional.of(testRequest));
            when(securityContextService.getCurrentUsername()).thenReturn("director@luxury.com");
            when(requestRepository.save(any(ExperientialRequest.class))).thenReturn(testRequest);

            conciergeService.fulfillRequest(testRequestId);

            assertEquals(ExperientialRequest.RequestState.Fulfilled, testRequest.getState());
            verify(eventPublisher).publishEvent(eventCaptor.capture());
            assertEquals(ConciergeRequestEvent.EventType.FULFILLED, eventCaptor.getValue().getEventType());
        }

        @Test
        @DisplayName("Should throw when fulfilling already-fulfilled request")
        void shouldThrowWhenAlreadyFulfilled() {
            testRequest.setState(ExperientialRequest.RequestState.Fulfilled);
            when(requestRepository.findActiveById(testRequestId)).thenReturn(Optional.of(testRequest));

            assertThrows(IllegalStateException.class,
                    () -> conciergeService.fulfillRequest(testRequestId));

            verify(eventPublisher, never()).publishEvent(any());
        }

        @Test
        @DisplayName("Should throw when fulfilling cancelled request")
        void shouldThrowWhenCancelled() {
            testRequest.setState(ExperientialRequest.RequestState.Cancelled);
            when(requestRepository.findActiveById(testRequestId)).thenReturn(Optional.of(testRequest));

            assertThrows(IllegalStateException.class,
                    () -> conciergeService.fulfillRequest(testRequestId));
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  Alerts
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Concierge Alerts")
    class AlertTests {

        @Test
        @DisplayName("Should return unread alerts for associate")
        void shouldReturnUnreadAlerts() {
            UUID associateId = UUID.randomUUID();
            Pageable pageable = PageRequest.of(0, 20);

            ConciergeAlert alert = ConciergeAlert.builder()
                    .profileId(testProfileId)
                    .associateId(associateId)
                    .alertType("Upcoming_Anniversary")
                    .message("Anniversary in 21 days")
                    .suggestedItems(List.of("item-1", "item-2", "item-3"))
                    .isRead(false)
                    .build();
            alert.setId(UUID.randomUUID());
            alert.setCreatedAt(Instant.now());

            when(alertRepository.findUnreadByAssociateId(associateId, pageable))
                    .thenReturn(new PageImpl<>(List.of(alert)));

            var result = conciergeService.getUnreadAlerts(associateId, pageable);

            assertEquals(1, result.getTotalElements());
            assertEquals("Upcoming_Anniversary", result.getContent().get(0).getAlertType());
        }

        @Test
        @DisplayName("Should mark alert as read")
        void shouldMarkAlertAsRead() {
            UUID alertId = UUID.randomUUID();
            when(alertRepository.markAsRead(alertId)).thenReturn(1);

            conciergeService.markAlertAsRead(alertId);

            verify(alertRepository).markAsRead(alertId);
        }

        @Test
        @DisplayName("Should throw when marking non-existent alert as read")
        void shouldThrowWhenAlertNotFound() {
            UUID alertId = UUID.randomUUID();
            when(alertRepository.markAsRead(alertId)).thenReturn(0);

            assertThrows(EntityNotFoundException.class,
                    () -> conciergeService.markAlertAsRead(alertId));
        }
    }
}
