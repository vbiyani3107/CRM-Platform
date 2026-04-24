package com.luxury.concierge.integration;

import com.luxury.concierge.model.ConciergeAlert;
import com.luxury.concierge.repository.ConciergeAlertRepository;
import com.luxury.concierge.service.ProactiveGiftingJobService;
import com.luxury.core.persistence.integration.AbstractPostgresIT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for the Proactive Gifting Job using pgvector/pgvector:pg16.
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("ProactiveGiftingJob Integration Tests")
class ProactiveGiftingIT extends AbstractPostgresIT {

    @Autowired
    private ProactiveGiftingJobService giftingJobService;

    @Autowired
    private ConciergeAlertRepository alertRepository;

    @BeforeEach
    void setUp() {
        alertRepository.deleteAll();
    }

    @Test
    @DisplayName("Should generate and persist alert via batch job service")
    void shouldGenerateAndPersistAlert() {
        UUID profileId = UUID.randomUUID();
        UUID associateId = UUID.randomUUID();
        List<String> items = List.of("vault-a", "vault-b", "vault-c");

        ConciergeAlert alert = giftingJobService.generateAlert(
                profileId, associateId, "Upcoming_Anniversary",
                "Anniversary in 21 days — top picks attached.", items);

        assertNotNull(alert.getId());
        assertFalse(alert.getIsRead());

        ConciergeAlert persisted = alertRepository.findById(alert.getId()).orElseThrow();
        assertEquals("Upcoming_Anniversary", persisted.getAlertType());
        assertEquals(3, persisted.getSuggestedItems().size());
        assertNotNull(persisted.getCreatedAt());
    }

    @Test
    @DisplayName("Scheduled scan should run without errors against real DB")
    void scheduledScanShouldRun() {
        assertDoesNotThrow(() -> giftingJobService.scanAndGenerateAlerts());
    }
}
