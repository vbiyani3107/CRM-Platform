package com.luxury.concierge.integration;

import com.luxury.concierge.model.ConciergeAlert;
import com.luxury.concierge.model.ExperientialRequest;
import com.luxury.concierge.repository.ConciergeAlertRepository;
import com.luxury.concierge.repository.ExperientialRequestRepository;
import com.luxury.core.persistence.integration.AbstractPostgresIT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for Concierge Hub using pgvector/pgvector:pg16 Testcontainers.
 *
 * <p>CRITICAL: Uses pgvector/pgvector:pg16 (NOT plain postgres:16) because
 * the vector extension must be available for Flyway V1 migration.</p>
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Concierge Integration Tests")
class ConciergeIT extends AbstractPostgresIT {

    @Autowired
    private ExperientialRequestRepository requestRepository;

    @Autowired
    private ConciergeAlertRepository alertRepository;

    private UUID profileId;

    @BeforeEach
    void setUp() {
        alertRepository.deleteAll();
        requestRepository.deleteAll();
        profileId = UUID.randomUUID();
    }

    @Nested
    @DisplayName("ExperientialRequest CRUD")
    class RequestCrudTests {

        @Test
        @DisplayName("Should persist and retrieve experiential request")
        void shouldPersistAndRetrieve() {
            ExperientialRequest request = ExperientialRequest.builder()
                    .number("EXP0000001")
                    .profileId(profileId)
                    .requestType("Yacht Charter")
                    .discretionLevel(ExperientialRequest.DiscretionLevel.High)
                    .state(ExperientialRequest.RequestState.Open)
                    .conciergeNotes("Mediterranean route preferred")
                    .targetDate(LocalDate.now().plusDays(30))
                    .customAttributes(Map.of("yacht_size", "50m+"))
                    .build();
            request.setCreatedBy("test-user");
            request.setUpdatedBy("test-user");

            ExperientialRequest saved = requestRepository.save(request);

            assertNotNull(saved.getId());
            assertEquals("EXP0000001", saved.getNumber());

            Optional<ExperientialRequest> found = requestRepository.findActiveById(saved.getId());
            assertTrue(found.isPresent());
            assertEquals("Yacht Charter", found.get().getRequestType());
            assertEquals("50m+", found.get().getCustomAttributes().get("yacht_size"));
        }

        @Test
        @DisplayName("Should find active requests with pagination")
        void shouldFindActiveWithPagination() {
            for (int i = 1; i <= 5; i++) {
                ExperientialRequest r = ExperientialRequest.builder()
                        .number(String.format("EXP%07d", i))
                        .profileId(profileId)
                        .requestType("Type " + i)
                        .build();
                r.setCreatedBy("test");
                r.setUpdatedBy("test");
                requestRepository.save(r);
            }

            Page<ExperientialRequest> page = requestRepository.findAllActive(PageRequest.of(0, 3));
            assertEquals(3, page.getContent().size());
            assertEquals(5, page.getTotalElements());
        }

        @Test
        @DisplayName("Should exclude soft-deleted requests")
        void shouldExcludeSoftDeleted() {
            ExperientialRequest request = ExperientialRequest.builder()
                    .number("EXP0000099")
                    .profileId(profileId)
                    .requestType("Deleted Request")
                    .build();
            request.setCreatedBy("test");
            request.setUpdatedBy("test");
            request.setIsDeleted(true);

            requestRepository.save(request);

            Page<ExperientialRequest> active = requestRepository.findAllActive(PageRequest.of(0, 20));
            assertTrue(active.getContent().stream()
                    .noneMatch(r -> "EXP0000099".equals(r.getNumber())));
        }

        @Test
        @DisplayName("Should persist JSONB custom attributes")
        void shouldPersistJsonbAttributes() {
            Map<String, Object> attrs = Map.of(
                    "venue", "Monaco Yacht Club",
                    "guests", 12,
                    "dietary", List.of("vegan", "gluten-free")
            );

            ExperientialRequest request = ExperientialRequest.builder()
                    .number("EXP0000010")
                    .profileId(profileId)
                    .requestType("Private Dining")
                    .customAttributes(attrs)
                    .build();
            request.setCreatedBy("test");
            request.setUpdatedBy("test");

            ExperientialRequest saved = requestRepository.save(request);
            ExperientialRequest found = requestRepository.findById(saved.getId()).orElseThrow();

            assertEquals("Monaco Yacht Club", found.getCustomAttributes().get("venue"));
        }

        @Test
        @DisplayName("Should auto-generate sequence number")
        void shouldAutoGenerateSequence() {
            ExperientialRequest r1 = ExperientialRequest.builder()
                    .number("EXP0000001").profileId(profileId).build();
            r1.setCreatedBy("t"); r1.setUpdatedBy("t");
            requestRepository.save(r1);

            int nextSeq = requestRepository.getNextSequenceNumber();
            assertEquals(2, nextSeq);
        }
    }

    @Nested
    @DisplayName("ConciergeAlert CRUD")
    class AlertCrudTests {

        @Test
        @DisplayName("Should persist and retrieve alert")
        void shouldPersistAlert() {
            UUID associateId = UUID.randomUUID();

            ConciergeAlert alert = ConciergeAlert.builder()
                    .profileId(profileId)
                    .associateId(associateId)
                    .alertType("Upcoming_Anniversary")
                    .message("Anniversary in 21 days")
                    .suggestedItems(List.of("item-1", "item-2"))
                    .isRead(false)
                    .build();

            ConciergeAlert saved = alertRepository.save(alert);
            assertNotNull(saved.getId());
            assertNotNull(saved.getCreatedAt());

            Page<ConciergeAlert> unread = alertRepository
                    .findUnreadByAssociateId(associateId, PageRequest.of(0, 10));
            assertEquals(1, unread.getTotalElements());
        }

        @Test
        @DisplayName("Should mark alert as read")
        void shouldMarkAsRead() {
            UUID associateId = UUID.randomUUID();
            ConciergeAlert alert = ConciergeAlert.builder()
                    .profileId(profileId)
                    .associateId(associateId)
                    .alertType("Taste_Match_Arrival")
                    .message("New arrival")
                    .isRead(false)
                    .build();

            ConciergeAlert saved = alertRepository.save(alert);
            int updated = alertRepository.markAsRead(saved.getId());
            assertEquals(1, updated);

            Page<ConciergeAlert> unread = alertRepository
                    .findUnreadByAssociateId(associateId, PageRequest.of(0, 10));
            assertEquals(0, unread.getTotalElements());
        }

        @Test
        @DisplayName("Should persist JSONB suggested items")
        void shouldPersistJsonbItems() {
            ConciergeAlert alert = ConciergeAlert.builder()
                    .profileId(profileId)
                    .associateId(UUID.randomUUID())
                    .alertType("Upcoming_Anniversary")
                    .suggestedItems(List.of("a", "b", "c"))
                    .build();

            ConciergeAlert saved = alertRepository.save(alert);
            ConciergeAlert found = alertRepository.findById(saved.getId()).orElseThrow();
            assertEquals(3, found.getSuggestedItems().size());
        }
    }
}
