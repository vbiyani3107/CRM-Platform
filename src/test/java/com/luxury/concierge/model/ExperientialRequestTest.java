package com.luxury.concierge.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Unit tests for {@link ExperientialRequest} entity.
 */
@DisplayName("ExperientialRequest Entity Tests")
class ExperientialRequestTest {

    @Test
    @DisplayName("Should create request with builder defaults")
    void shouldCreateWithBuilderDefaults() {
        ExperientialRequest request = ExperientialRequest.builder()
                .number("EXP0000001")
                .profileId(UUID.randomUUID())
                .requestType("Yacht Charter")
                .build();

        assertEquals("EXP0000001", request.getNumber());
        assertEquals("Yacht Charter", request.getRequestType());
        assertEquals(ExperientialRequest.DiscretionLevel.High, request.getDiscretionLevel());
        assertEquals(ExperientialRequest.RequestState.Open, request.getState());
        assertNotNull(request.getCustomAttributes());
    }

    @Test
    @DisplayName("Should set all fields via builder")
    void shouldSetAllFieldsViaBuilder() {
        UUID profileId = UUID.randomUUID();
        LocalDate targetDate = LocalDate.now().plusDays(30);
        Map<String, Object> attrs = Map.of("venue", "Monaco Yacht Club");

        ExperientialRequest request = ExperientialRequest.builder()
                .number("EXP0000042")
                .profileId(profileId)
                .requestType("Private Viewing")
                .discretionLevel(ExperientialRequest.DiscretionLevel.Phantom)
                .state(ExperientialRequest.RequestState.In_Progress)
                .conciergeNotes("VIP client requires absolute privacy")
                .targetDate(targetDate)
                .customAttributes(attrs)
                .build();

        assertEquals(profileId, request.getProfileId());
        assertEquals("Private Viewing", request.getRequestType());
        assertEquals(ExperientialRequest.DiscretionLevel.Phantom, request.getDiscretionLevel());
        assertEquals(ExperientialRequest.RequestState.In_Progress, request.getState());
        assertEquals("VIP client requires absolute privacy", request.getConciergeNotes());
        assertEquals(targetDate, request.getTargetDate());
        assertEquals("Monaco Yacht Club", request.getCustomAttributes().get("venue"));
    }

    @Test
    @DisplayName("Should have correct enum values for DiscretionLevel")
    void shouldHaveCorrectDiscretionLevels() {
        ExperientialRequest.DiscretionLevel[] levels = ExperientialRequest.DiscretionLevel.values();
        assertEquals(3, levels.length);
        assertEquals(ExperientialRequest.DiscretionLevel.High, levels[0]);
        assertEquals(ExperientialRequest.DiscretionLevel.Maximum, levels[1]);
        assertEquals(ExperientialRequest.DiscretionLevel.Phantom, levels[2]);
    }

    @Test
    @DisplayName("Should have correct enum values for RequestState")
    void shouldHaveCorrectRequestStates() {
        ExperientialRequest.RequestState[] states = ExperientialRequest.RequestState.values();
        assertEquals(4, states.length);
        assertEquals(ExperientialRequest.RequestState.Open, states[0]);
        assertEquals(ExperientialRequest.RequestState.In_Progress, states[1]);
        assertEquals(ExperientialRequest.RequestState.Fulfilled, states[2]);
        assertEquals(ExperientialRequest.RequestState.Cancelled, states[3]);
    }

    @Test
    @DisplayName("BaseEntity isDeleted should default to false")
    void shouldDefaultToNotDeleted() {
        ExperientialRequest request = ExperientialRequest.builder()
                .number("EXP0000099")
                .profileId(UUID.randomUUID())
                .build();

        assertFalse(request.getIsDeleted());
    }
}
