package com.luxury.core.api.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

/**
 * Request DTO for creating or updating a dynamic entity.
 *
 * <p>The {@code attributes} map contains the entity's column values as
 * key-value pairs. Validation against {@code sys_dictionary} definitions
 * is performed by the {@code DynamicPayloadValidator}.</p>
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenericEntityRequest {

    @NotEmpty(message = "Attributes must not be empty")
    private Map<String, Object> attributes;
}
