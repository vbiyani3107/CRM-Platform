package com.luxury.core.persistence.repository;

import com.luxury.core.persistence.model.SysDictionary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for the Attribute Registry (sys_dictionary).
 * Supports querying field definitions by entity and sensitivity filtering.
 */
@Repository
public interface SysDictionaryRepository extends JpaRepository<SysDictionary, UUID> {

    /**
     * Find all attribute definitions for a given entity.
     */
    List<SysDictionary> findByEntityName(String entityName);

    /**
     * Find only sensitive attributes for a given entity (used by FLS interceptor).
     */
    List<SysDictionary> findByEntityNameAndIsSensitiveTrue(String entityName);

    /**
     * Find only custom (dynamically added) attributes for a given entity.
     */
    List<SysDictionary> findByEntityNameAndIsCustomTrue(String entityName);

    /**
     * Check if a specific attribute exists on an entity.
     */
    boolean existsByEntityNameAndAttributeName(String entityName, String attributeName);

    /**
     * Native query to filter dictionary entries by JSONB validation_rules content.
     * Example: find all fields where validation_rules contains a specific key.
     */
    @Query(value = """
            SELECT d.* FROM luxury_brand.sys_dictionary d
            WHERE d.entity_name = :entityName
              AND d.validation_rules @> CAST(:jsonFilter AS jsonb)
            """, nativeQuery = true)
    List<SysDictionary> findByEntityNameAndValidationRulesContaining(
            @Param("entityName") String entityName,
            @Param("jsonFilter") String jsonFilter
    );
}
