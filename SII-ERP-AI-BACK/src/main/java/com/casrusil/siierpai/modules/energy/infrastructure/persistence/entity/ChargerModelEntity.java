package com.casrusil.siierpai.modules.energy.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "charger_models", schema = "energy")
public class ChargerModelEntity {

    @Id
    private UUID id;

    @Column(name = "manufacturer_id", nullable = false)
    private UUID manufacturerId;

    @Column(name = "model_name", nullable = false)
    private String modelName;

    @Column(name = "model_number")
    private String modelNumber;

    @Column(name = "number_of_connectors", nullable = false)
    private int numberOfConnectors;

    @Column(name = "max_power_kw")
    private BigDecimal maxPowerKw;

    @Column(name = "firmware_version")
    private String firmwareVersion;

    @Column(name = "ocpp_version")
    private String ocppVersion;

    @Column(name = "supports_smart_charging")
    private boolean supportsSmartCharging;

    @Column(name = "supports_reservation")
    private boolean supportsReservation;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public ChargerModelEntity() {}

    public ChargerModelEntity(UUID id, UUID manufacturerId, String modelName, String modelNumber, int numberOfConnectors, BigDecimal maxPowerKw, String firmwareVersion, String ocppVersion, boolean supportsSmartCharging, boolean supportsReservation, boolean active, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.manufacturerId = manufacturerId;
        this.modelName = modelName;
        this.modelNumber = modelNumber;
        this.numberOfConnectors = numberOfConnectors;
        this.maxPowerKw = maxPowerKw;
        this.firmwareVersion = firmwareVersion;
        this.ocppVersion = ocppVersion;
        this.supportsSmartCharging = supportsSmartCharging;
        this.supportsReservation = supportsReservation;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getManufacturerId() { return manufacturerId; }
    public void setManufacturerId(UUID manufacturerId) { this.manufacturerId = manufacturerId; }

    public String getModelName() { return modelName; }
    public void setModelName(String modelName) { this.modelName = modelName; }

    public String getModelNumber() { return modelNumber; }
    public void setModelNumber(String modelNumber) { this.modelNumber = modelNumber; }

    public int getNumberOfConnectors() { return numberOfConnectors; }
    public void setNumberOfConnectors(int numberOfConnectors) { this.numberOfConnectors = numberOfConnectors; }

    public BigDecimal getMaxPowerKw() { return maxPowerKw; }
    public void setMaxPowerKw(BigDecimal maxPowerKw) { this.maxPowerKw = maxPowerKw; }

    public String getFirmwareVersion() { return firmwareVersion; }
    public void setFirmwareVersion(String firmwareVersion) { this.firmwareVersion = firmwareVersion; }

    public String getOcppVersion() { return ocppVersion; }
    public void setOcppVersion(String ocppVersion) { this.ocppVersion = ocppVersion; }

    public boolean isSupportsSmartCharging() { return supportsSmartCharging; }
    public void setSupportsSmartCharging(boolean supportsSmartCharging) { this.supportsSmartCharging = supportsSmartCharging; }

    public boolean isSupportsReservation() { return supportsReservation; }
    public void setSupportsReservation(boolean supportsReservation) { this.supportsReservation = supportsReservation; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
