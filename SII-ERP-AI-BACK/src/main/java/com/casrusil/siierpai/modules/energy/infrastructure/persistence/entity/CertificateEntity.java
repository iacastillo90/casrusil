package com.casrusil.siierpai.modules.energy.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA Entity for X.509 Certificates.
 * 
 * <p>
 * Maps to the energy.certificates table for persistent storage
 * of OCPP security certificates.
 * 
 * @since 1.0
 */
@Entity
@Table(name = "certificates", schema = "energy")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CertificateEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "station_id", nullable = false)
    private UUID stationId;

    @Column(name = "type", nullable = false, length = 50)
    private String type;

    @Column(name = "certificate_data", nullable = false, columnDefinition = "TEXT")
    private String certificateData;

    @Column(name = "serial_number", length = 255)
    private String serialNumber;

    @Column(name = "issuer", length = 255)
    private String issuer;

    @Column(name = "valid_from")
    private LocalDateTime validFrom;

    @Column(name = "valid_to")
    private LocalDateTime validTo;

    @Column(name = "status", nullable = false, length = 50)
    private String status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
