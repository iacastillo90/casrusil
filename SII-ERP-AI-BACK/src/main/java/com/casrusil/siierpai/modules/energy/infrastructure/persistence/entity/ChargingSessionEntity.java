package com.casrusil.siierpai.modules.energy.infrastructure.persistence.entity;

import com.casrusil.siierpai.modules.energy.domain.model.ChargingSessionStatus;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidad JPA: Sesión de Carga.
 * Mapea la entidad de dominio ChargingSession a la base de datos.
 */
@Entity
@Table(name = "charging_sessions")
public class ChargingSessionEntity {

    @Id
    private UUID id;

    @Column(name = "station_id", nullable = false)
    private UUID stationId;

    @Column(name = "charger_ocpp_id", nullable = false)
    private String chargerOcppId;

    @Column(name = "connector_number", nullable = false)
    private Integer connectorNumber;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChargingSessionStatus status;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Column(name = "initial_energy_kwh", precision = 10, scale = 3)
    private BigDecimal initialEnergyKwh;

    @Column(name = "final_energy_kwh", precision = 10, scale = 3)
    private BigDecimal finalEnergyKwh;

    @Column(name = "stop_reason", length = 500)
    private String stopReason;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EnergyMeasurementEntity> measurements = new ArrayList<>();

    protected ChargingSessionEntity() {
        // Constructor vacío requerido por JPA
    }

    public ChargingSessionEntity(UUID id, UUID stationId, String chargerOcppId,
            Integer connectorNumber, UUID userId,
            ChargingSessionStatus status, LocalDateTime startedAt) {
        this.id = id;
        this.stationId = stationId;
        this.chargerOcppId = chargerOcppId;
        this.connectorNumber = connectorNumber;
        this.userId = userId;
        this.status = status;
        this.startedAt = startedAt;
    }

    // Helper methods para gestionar la relación bidireccional

    public void addMeasurement(EnergyMeasurementEntity measurement) {
        measurements.add(measurement);
        measurement.setSession(this);
    }

    public void removeMeasurement(EnergyMeasurementEntity measurement) {
        measurements.remove(measurement);
        measurement.setSession(null);
    }

    // Getters y Setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getStationId() {
        return stationId;
    }

    public void setStationId(UUID stationId) {
        this.stationId = stationId;
    }

    public String getChargerOcppId() {
        return chargerOcppId;
    }

    public void setChargerOcppId(String chargerOcppId) {
        this.chargerOcppId = chargerOcppId;
    }

    public Integer getConnectorNumber() {
        return connectorNumber;
    }

    public void setConnectorNumber(Integer connectorNumber) {
        this.connectorNumber = connectorNumber;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public ChargingSessionStatus getStatus() {
        return status;
    }

    public void setStatus(ChargingSessionStatus status) {
        this.status = status;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getEndedAt() {
        return endedAt;
    }

    public void setEndedAt(LocalDateTime endedAt) {
        this.endedAt = endedAt;
    }

    public BigDecimal getInitialEnergyKwh() {
        return initialEnergyKwh;
    }

    public void setInitialEnergyKwh(BigDecimal initialEnergyKwh) {
        this.initialEnergyKwh = initialEnergyKwh;
    }

    public BigDecimal getFinalEnergyKwh() {
        return finalEnergyKwh;
    }

    public void setFinalEnergyKwh(BigDecimal finalEnergyKwh) {
        this.finalEnergyKwh = finalEnergyKwh;
    }

    public String getStopReason() {
        return stopReason;
    }

    public void setStopReason(String stopReason) {
        this.stopReason = stopReason;
    }

    public List<EnergyMeasurementEntity> getMeasurements() {
        return measurements;
    }

    public void setMeasurements(List<EnergyMeasurementEntity> measurements) {
        this.measurements = measurements;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ChargingSessionEntity that = (ChargingSessionEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
