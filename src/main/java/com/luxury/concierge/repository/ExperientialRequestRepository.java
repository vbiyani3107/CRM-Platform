package com.luxury.concierge.repository;

import com.luxury.concierge.model.ExperientialRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for {@link ExperientialRequest} entities.
 *
 * <p>All queries exclude soft-deleted records by default.</p>
 */
@Repository
public interface ExperientialRequestRepository extends JpaRepository<ExperientialRequest, UUID> {

    /**
     * Finds all non-deleted experiential requests with pagination.
     */
    @Query("SELECT e FROM ExperientialRequest e WHERE e.isDeleted = false ORDER BY e.createdAt DESC")
    Page<ExperientialRequest> findAllActive(Pageable pageable);

    /**
     * Finds all non-deleted requests for a specific profile.
     */
    @Query("SELECT e FROM ExperientialRequest e WHERE e.profileId = :profileId AND e.isDeleted = false ORDER BY e.createdAt DESC")
    Page<ExperientialRequest> findByProfileId(@Param("profileId") UUID profileId, Pageable pageable);

    /**
     * Finds a non-deleted request by ID.
     */
    @Query("SELECT e FROM ExperientialRequest e WHERE e.id = :id AND e.isDeleted = false")
    Optional<ExperientialRequest> findActiveById(@Param("id") UUID id);

    /**
     * Finds the next available sequence number for auto-numbering.
     */
    @Query("SELECT COALESCE(MAX(CAST(SUBSTRING(e.number, 4) AS int)), 0) + 1 FROM ExperientialRequest e")
    int getNextSequenceNumber();
}
