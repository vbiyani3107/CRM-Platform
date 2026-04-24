package com.luxury.core.persistence.model;

import com.luxury.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;

/**
 * Attribute Registry — defines column types (string, boolean, jsonb, vector, reference)
 * for each entity. Enforces validation before writes.
 *
 * Maps to the `sys_dictionary` table in the `luxury_brand` schema.
 */
@Entity
@Table(name = "sys_dictionary", schema = "luxury_brand")
@Getter
@Setter
@NoArgsConstructor
public class SysDictionary extends BaseEntity {

    @NotBlank
    @Column(name = "entity_name", nullable = false, length = 100)
    private String entityName;

    @NotBlank
    @Column(name = "attribute_name", nullable = false, length = 100)
    private String attributeName;

    @NotBlank
    @Column(name = "data_type", nullable = false, length = 20)
    private String dataType;

    @Column(name = "is_custom")
    private Boolean isCustom = false;

    @Column(name = "is_sensitive")
    private Boolean isSensitive = false;

    @Column(name = "is_required")
    private Boolean isRequired = false;

    @Column(name = "default_value", length = 500)
    private String defaultValue;

    @Column(name = "max_length")
    private Integer maxLength;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "validation_rules", columnDefinition = "jsonb")
    private Map<String, Object> validationRules;
}
