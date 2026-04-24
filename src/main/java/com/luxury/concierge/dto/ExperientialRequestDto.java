package com.luxury.concierge.dto;

import com.luxury.concierge.model.ExperientialRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

/**
 * Request DTO for creating/updating experiential requests.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExperientialRequestDto {

    @NotNull(message = "Profile ID is required")
    private UUID profileId;

    @NotBlank(message = "Request type is required")
    private String requestType;

    private ExperientialRequest.DiscretionLevel discretionLevel;

    private String conciergeNotes;

    private LocalDate targetDate;

    private Map<String, Object> customAttributes;
}
