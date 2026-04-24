package com.luxury.core.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Response DTO for dynamic entity CRUD results.
 *
 * <p>Wraps a single entity row with its metadata. This is the payload
 * embedded inside {@code ApiResponse<GenericEntityResponse>}.</p>
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenericEntityResponse {

    private UUID id;
    private String entityName;
    private Map<String, Object> attributes;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
}
