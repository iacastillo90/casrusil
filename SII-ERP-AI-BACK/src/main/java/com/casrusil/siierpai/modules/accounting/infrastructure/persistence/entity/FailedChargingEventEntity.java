package com.casrusil.siierpai.modules.accounting.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * Cola de mensajes muertos (Dead Letter Queue) persistente para los eventos de
 * carga cuyo asiento contable no pudo generarse.
 *
 * <p>En lugar de perder el registro (y por tanto el ingreso a facturar), cada
 * fallo se guarda aqui con su motivo para poder reprocesarlo manualmente o por
 * un job de reintentos. Asi se cumple el requisito de "no perder dinero".
 */
@Entity
@Table(name = "failed_charging_events")
public class FailedChargingEventEntity {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "session_id", nullable = false)
    private String sessionId;

    @Column(name = "total_energy_kwh")
    private String totalEnergyKwh;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "error_message", length = 2000)
    private String errorMessage;

    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    @Column(name = "resolved", nullable = false)
    private boolean resolved;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected FailedChargingEventEntity() {
        // JPA
    }

    public FailedChargingEventEntity(String sessionId, String totalEnergyKwh, String userId, String errorMessage) {
        this.id = UUID.randomUUID();
        this.sessionId = sessionId;
        this.totalEnergyKwh = totalEnergyKwh;
        this.userId = userId;
        this.errorMessage = errorMessage;
        this.retryCount = 0;
        this.resolved = false;
        this.createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public String getSessionId() { return sessionId; }
    public String getTotalEnergyKwh() { return totalEnergyKwh; }
    public String getUserId() { return userId; }
    public String getErrorMessage() { return errorMessage; }
    public int getRetryCount() { return retryCount; }
    public boolean isResolved() { return resolved; }
    public Instant getCreatedAt() { return createdAt; }

    public void incrementRetry() { this.retryCount++; }
    public void markResolved() { this.resolved = true; }
}
