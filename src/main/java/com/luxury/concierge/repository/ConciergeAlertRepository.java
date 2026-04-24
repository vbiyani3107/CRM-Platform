package com.luxury.concierge.repository;

import com.luxury.concierge.model.ConciergeAlert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repository for {@link ConciergeAlert} entities.
 */
@Repository
public interface ConciergeAlertRepository extends JpaRepository<ConciergeAlert, UUID> {

    /**
     * Finds all unread alerts for a specific associate, ordered newest first.
     */
    @Query("SELECT a FROM ConciergeAlert a WHERE a.associateId = :associateId AND a.isRead = false ORDER BY a.createdAt DESC")
    Page<ConciergeAlert> findUnreadByAssociateId(@Param("associateId") UUID associateId, Pageable pageable);

    /**
     * Finds all alerts for a specific profile.
     */
    @Query("SELECT a FROM ConciergeAlert a WHERE a.profileId = :profileId ORDER BY a.createdAt DESC")
    Page<ConciergeAlert> findByProfileId(@Param("profileId") UUID profileId, Pageable pageable);

    /**
     * Marks an alert as read.
     */
    @Modifying
    @Query("UPDATE ConciergeAlert a SET a.isRead = true WHERE a.id = :id")
    int markAsRead(@Param("id") UUID id);
}
