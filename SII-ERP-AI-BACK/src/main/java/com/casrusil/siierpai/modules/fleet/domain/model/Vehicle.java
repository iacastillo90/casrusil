package com.casrusil.siierpai.modules.fleet.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidad de Dominio: Vehículo de la Flota.
 * Representa un vehículo eléctrico que puede ser cargado en las estaciones.
 */
public class Vehicle {

    private final UUID id;
    private final String licensePlate;
    private final String brand;
    private final String model;
    private final Integer year;
    private final String rfidTag;
    private VehicleStatus status;
    private Integer batteryLevel; // 0-100%
    private UUID currentChargingStationId;
    private LocalDateTime lastChargingSession;
    private final LocalDateTime createdAt;

    private Vehicle(Builder builder) {
        this.id = Objects.requireNonNull(builder.id, "Vehicle ID cannot be null");
        this.licensePlate = Objects.requireNonNull(builder.licensePlate, "License plate cannot be null");
        this.brand = builder.brand;
        this.model = builder.model;
        this.year = builder.year;
        this.rfidTag = builder.rfidTag;
        this.status = builder.status != null ? builder.status : VehicleStatus.AVAILABLE;
        this.batteryLevel = builder.batteryLevel;
        this.currentChargingStationId = builder.currentChargingStationId;
        this.lastChargingSession = builder.lastChargingSession;
        this.createdAt = builder.createdAt != null ? builder.createdAt : LocalDateTime.now();
    }

    // ========== DOMAIN BEHAVIOR ==========

    public void startCharging(UUID stationId) {
        if (this.status == VehicleStatus.CHARGING) {
            throw new IllegalStateException("Vehicle is already charging");
        }
        this.status = VehicleStatus.CHARGING;
        this.currentChargingStationId = stationId;
    }

    public void completeCharging(Integer finalBatteryLevel) {
        if (this.status != VehicleStatus.CHARGING) {
            throw new IllegalStateException("Vehicle is not charging");
        }
        this.status = VehicleStatus.AVAILABLE;
        this.batteryLevel = finalBatteryLevel;
        this.currentChargingStationId = null;
        this.lastChargingSession = LocalDateTime.now();
    }

    public void setOutOfService() {
        this.status = VehicleStatus.OUT_OF_SERVICE;
    }

    public void setAvailable() {
        this.status = VehicleStatus.AVAILABLE;
    }

    // ========== GETTERS ==========

    public UUID getId() {
        return id;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public String getBrand() {
        return brand;
    }

    public String getModel() {
        return model;
    }

    public Integer getYear() {
        return year;
    }

    public String getRfidTag() {
        return rfidTag;
    }

    public VehicleStatus getStatus() {
        return status;
    }

    public Integer getBatteryLevel() {
        return batteryLevel;
    }

    public UUID getCurrentChargingStationId() {
        return currentChargingStationId;
    }

    public LocalDateTime getLastChargingSession() {
        return lastChargingSession;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // ========== BUILDER ==========

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private String licensePlate;
        private String brand;
        private String model;
        private Integer year;
        private String rfidTag;
        private VehicleStatus status;
        private Integer batteryLevel;
        private UUID currentChargingStationId;
        private LocalDateTime lastChargingSession;
        private LocalDateTime createdAt;

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder licensePlate(String licensePlate) {
            this.licensePlate = licensePlate;
            return this;
        }

        public Builder brand(String brand) {
            this.brand = brand;
            return this;
        }

        public Builder model(String model) {
            this.model = model;
            return this;
        }

        public Builder year(Integer year) {
            this.year = year;
            return this;
        }

        public Builder rfidTag(String rfidTag) {
            this.rfidTag = rfidTag;
            return this;
        }

        public Builder status(VehicleStatus status) {
            this.status = status;
            return this;
        }

        public Builder batteryLevel(Integer batteryLevel) {
            this.batteryLevel = batteryLevel;
            return this;
        }

        public Builder currentChargingStationId(UUID currentChargingStationId) {
            this.currentChargingStationId = currentChargingStationId;
            return this;
        }

        public Builder lastChargingSession(LocalDateTime lastChargingSession) {
            this.lastChargingSession = lastChargingSession;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Vehicle build() {
            return new Vehicle(this);
        }
    }
}
