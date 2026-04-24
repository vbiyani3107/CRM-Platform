package com.luxury.concierge.model;

import com.luxury.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

/**
 * Represents a luxury experiential request (e.g., Yacht Charter, Private Viewing).
 *
 * <p>Each request is linked to a HyperProfile and carries discretion levels
 * that determine encryption requirements. Requests marked as {@code Phantom}
 * have their notes and custom attributes encrypted at the application level.</p>
 */
@Entity
@Table(name = "experiential_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExperientialRequest extends BaseEntity {

    @Column(name = "number", nullable = false, unique = true, length = 20)
    private String number;

    @Column(name = "profile_id", nullable = false)
    private UUID profileId;

    @Column(name = "request_type", length = 100)
    private String requestType;

    @Enumerated(EnumType.STRING)
    @Column(name = "discretion_level", length = 50)
    @Builder.Default
    private DiscretionLevel discretionLevel = DiscretionLevel.High;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", length = 50)
    @Builder.Default
    private RequestState state = RequestState.Open;

    @Column(name = "concierge_notes", columnDefinition = "TEXT")
    private String conciergeNotes;

    @Column(name = "target_date")
    private LocalDate targetDate;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "custom_attributes", columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, Object> customAttributes = Map.of();

    /**
     * Discretion levels controlling encryption and access.
     */
    public enum DiscretionLevel {
        High, Maximum, Phantom
    }

    /**
     * Lifecycle states for experiential requests.
     */
    public enum RequestState {
        Open, In_Progress, Fulfilled, Cancelled
    }
}
