package com.luxury.core.persistence.repository;

import com.luxury.core.persistence.model.SysDbObject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for the Entity Registry (sys_db_object).
 * Provides standard CRUD plus lookup by logical entity name.
 */
@Repository
public interface SysDbObjectRepository extends JpaRepository<SysDbObject, UUID> {

    Optional<SysDbObject> findByName(String name);

    boolean existsByName(String name);
}
