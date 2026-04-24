package com.luxury.concierge.service;

import com.luxury.concierge.model.ConciergeAlert;
import com.luxury.concierge.repository.ConciergeAlertRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProactiveGiftingJobService Unit Tests")
class ProactiveGiftingJobServiceTest {

    @Mock
    private ConciergeAlertRepository alertRepository;

    @InjectMocks
    private ProactiveGiftingJobService giftingJobService;

    @Test
    @DisplayName("Should generate alert with correct fields")
    void shouldGenerateAlert() {
        UUID profileId = UUID.randomUUID();
        UUID associateId = UUID.randomUUID();
        List<String> items = List.of("vault-1", "vault-2", "vault-3");

        when(alertRepository.save(any(ConciergeAlert.class))).thenAnswer(inv -> {
            ConciergeAlert a = inv.getArgument(0);
            a.setId(UUID.randomUUID());
            return a;
        });

        ConciergeAlert result = giftingJobService.generateAlert(
                profileId, associateId, "Upcoming_Anniversary",
                "Anniversary in 21 days", items);

        assertNotNull(result.getId());
        assertEquals(profileId, result.getProfileId());
        assertEquals("Upcoming_Anniversary", result.getAlertType());
        assertEquals(3, result.getSuggestedItems().size());
        assertFalse(result.getIsRead());
    }

    @Test
    @DisplayName("Should save alert via repository")
    void shouldSaveViaRepository() {
        when(alertRepository.save(any())).thenAnswer(inv -> {
            ConciergeAlert a = inv.getArgument(0);
            a.setId(UUID.randomUUID());
            return a;
        });

        giftingJobService.generateAlert(UUID.randomUUID(), UUID.randomUUID(),
                "Taste_Match_Arrival", "New arrival", List.of("i1"));

        ArgumentCaptor<ConciergeAlert> cap = ArgumentCaptor.forClass(ConciergeAlert.class);
        verify(alertRepository).save(cap.capture());
        assertEquals("Taste_Match_Arrival", cap.getValue().getAlertType());
    }

    @Test
    @DisplayName("Scheduled scan should execute without errors (stub mode)")
    void scheduledScanStub() {
        giftingJobService.scanAndGenerateAlerts();
    }
}
