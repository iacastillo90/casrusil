package com.casrusil.siierpai.modules.energy.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA Entity for OCPP Security Events.
 * 
 * <p>
 * Maps to the energy.security_events table for persistent storage
 * of security-related events from charging stations.
 * 
 * @since 1.0
 */
@Entity
@Table(name = "security_events", schema = "energy")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SecurityEventEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "station_id", nullable = false)
    private UUID stationId;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "tech_info", columnDefinition = "TEXT")
    private String techInfo;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
}
