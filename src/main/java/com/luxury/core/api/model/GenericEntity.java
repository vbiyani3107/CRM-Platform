package com.luxury.core.api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Lightweight runtime representation of a dynamic entity row.
 *
 * <p>This is NOT a JPA entity — it is a transfer object representing a row
 * from any table registered in {@code sys_db_object}. The attribute map holds
 * column values as key-value pairs, validated at runtime against
 * {@code sys_dictionary} definitions.</p>
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenericEntity {

    private UUID id;
    private String entityName;
    private Map<String, Object> attributes;

    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
    private Long version;
    private Boolean isDeleted;
}
