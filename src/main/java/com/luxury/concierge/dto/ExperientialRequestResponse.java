package com.luxury.concierge.dto;

import com.luxury.concierge.model.ExperientialRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

/**
 * Response DTO for experiential request data.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExperientialRequestResponse {

    private UUID id;
    private String number;
    private UUID profileId;
    private String requestType;
    private ExperientialRequest.DiscretionLevel discretionLevel;
    private ExperientialRequest.RequestState state;
    private String conciergeNotes;
    private LocalDate targetDate;
    private Map<String, Object> customAttributes;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
}
