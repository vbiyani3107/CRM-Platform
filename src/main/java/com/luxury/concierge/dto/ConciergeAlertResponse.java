package com.luxury.concierge.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for concierge alert data.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConciergeAlertResponse {

    private UUID id;
    private UUID profileId;
    private UUID associateId;
    private String alertType;
    private String message;
    private List<String> suggestedItems;
    private Boolean isRead;
    private Instant createdAt;
}
