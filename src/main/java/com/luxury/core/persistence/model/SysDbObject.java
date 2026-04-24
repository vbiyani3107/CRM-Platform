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
 * Entity Registry — defines core modules (hyper_profiles, the_vault, etc.)
 * and dynamically created bespoke tables.
 *
 * Maps to the `sys_db_object` table in the `luxury_brand` schema.
 */
@Entity
@Table(name = "sys_db_object", schema = "luxury_brand")
@Getter
@Setter
@NoArgsConstructor
public class SysDbObject extends BaseEntity {

    @NotBlank
    @Column(name = "name", unique = true, nullable = false, length = 100)
    private String name;

    @Column(name = "label", length = 255)
    private String label;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_extensible")
    private Boolean isExtensible = true;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "custom_attributes", columnDefinition = "jsonb")
    private Map<String, Object> customAttributes;
}
