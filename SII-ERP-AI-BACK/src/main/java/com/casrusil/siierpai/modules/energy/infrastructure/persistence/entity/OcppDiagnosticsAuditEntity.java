package com.casrusil.siierpai.modules.energy.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "ocpp_diagnostics_audit")
public class OcppDiagnosticsAuditEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "ocpp_id", nullable = false)
    private String ocppId;
    
    @Column(nullable = false)
    private String status;
    
    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    @Column(name = "event_type", nullable = false)
    private String eventType;
    
    protected OcppDiagnosticsAuditEntity() {}
    
    public OcppDiagnosticsAuditEntity(String ocppId, String status, LocalDateTime timestamp, String eventType) {
        this.ocppId = ocppId;
        this.status = status;
        this.timestamp = timestamp;
        this.eventType = eventType;
    }
    
    public UUID getId() { return id; }
    public String getOcppId() { return ocppId; }
    public String getStatus() { return status; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getEventType() { return eventType; }
}
