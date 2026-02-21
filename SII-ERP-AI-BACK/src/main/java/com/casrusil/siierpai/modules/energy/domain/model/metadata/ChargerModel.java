package com.casrusil.siierpai.modules.energy.domain.model.metadata;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Charger Model aggregate root.
 * 
 * Represents a specific charger model with its specifications.
 */
public class ChargerModel {
    private final ChargerModelId id;
    private final ManufacturerId manufacturerId;
    private String modelName;
    private String modelNumber;
    private int numberOfConnectors;
    private BigDecimal maxPowerKw;
    private String firmwareVersion;
    private String ocppVersion;
    private boolean supportsSmartCharging;
    private boolean supportsReservation;
    private boolean active;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private ChargerModel(Builder builder) {
        this.id = Objects.requireNonNull(builder.id, "Model ID is required");
        this.manufacturerId = Objects.requireNonNull(builder.manufacturerId, "Manufacturer ID is required");
        this.modelName = Objects.requireNonNull(builder.modelName, "Model name is required");
        this.modelNumber = builder.modelNumber;
        this.numberOfConnectors = builder.numberOfConnectors;
        this.maxPowerKw = builder.maxPowerKw;
        this.firmwareVersion = builder.firmwareVersion;
        this.ocppVersion = builder.ocppVersion;
        this.supportsSmartCharging = builder.supportsSmartCharging;
        this.supportsReservation = builder.supportsReservation;
        this.active = builder.active;
        this.createdAt = Objects.requireNonNull(builder.createdAt, "Created timestamp is required");
        this.updatedAt = builder.updatedAt;

        validateModel();
    }

    /**
     * Creates a new charger model.
     */
    public static ChargerModel create(
            ManufacturerId manufacturerId,
            String modelName,
            String modelNumber,
            int numberOfConnectors,
            BigDecimal maxPowerKw,
            String ocppVersion) {

        return builder()
                .id(ChargerModelId.generate())
                .manufacturerId(manufacturerId)
                .modelName(modelName)
                .modelNumber(modelNumber)
                .numberOfConnectors(numberOfConnectors)
                .maxPowerKw(maxPowerKw)
                .ocppVersion(ocppVersion)
                .supportsSmartCharging(false)
                .supportsReservation(false)
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Updates model specifications.
     */
    public void updateSpecifications(
            String modelName,
            String modelNumber,
            int numberOfConnectors,
            BigDecimal maxPowerKw) {

        if (modelName != null && !modelName.trim().isEmpty()) {
            this.modelName = modelName;
        }
        this.modelNumber = modelNumber;
        this.numberOfConnectors = numberOfConnectors;
        this.maxPowerKw = maxPowerKw;
        this.updatedAt = LocalDateTime.now();

        validateModel();
    }

    /**
     * Updates firmware version.
     */
    public void updateFirmware(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Enables smart charging support.
     */
    public void enableSmartCharging() {
        this.supportsSmartCharging = true;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Enables reservation support.
     */
    public void enableReservation() {
        this.supportsReservation = true;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Activates the model.
     */
    public void activate() {
        this.active = true;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Deactivates the model.
     */
    public void deactivate() {
        this.active = false;
        this.updatedAt = LocalDateTime.now();
    }

    private void validateModel() {
        if (numberOfConnectors < 1) {
            throw new IllegalArgumentException("Number of connectors must be at least 1");
        }
        if (maxPowerKw != null && maxPowerKw.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Max power must be positive");
        }
    }

    // Getters
    public ChargerModelId getId() {
        return id;
    }

    public ManufacturerId getManufacturerId() {
        return manufacturerId;
    }

    public String getModelName() {
        return modelName;
    }

    public String getModelNumber() {
        return modelNumber;
    }

    public int getNumberOfConnectors() {
        return numberOfConnectors;
    }

    public BigDecimal getMaxPowerKw() {
        return maxPowerKw;
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public String getOcppVersion() {
        return ocppVersion;
    }

    public boolean supportsSmartCharging() {
        return supportsSmartCharging;
    }

    public boolean supportsReservation() {
        return supportsReservation;
    }

    public boolean isActive() {
        return active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    // Builder
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private ChargerModelId id;
        private ManufacturerId manufacturerId;
        private String modelName;
        private String modelNumber;
        private int numberOfConnectors = 1;
        private BigDecimal maxPowerKw;
        private String firmwareVersion;
        private String ocppVersion;
        private boolean supportsSmartCharging = false;
        private boolean supportsReservation = false;
        private boolean active = true;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder id(ChargerModelId id) {
            this.id = id;
            return this;
        }

        public Builder manufacturerId(ManufacturerId manufacturerId) {
            this.manufacturerId = manufacturerId;
            return this;
        }

        public Builder modelName(String modelName) {
            this.modelName = modelName;
            return this;
        }

        public Builder modelNumber(String modelNumber) {
            this.modelNumber = modelNumber;
            return this;
        }

        public Builder numberOfConnectors(int numberOfConnectors) {
            this.numberOfConnectors = numberOfConnectors;
            return this;
        }

        public Builder maxPowerKw(BigDecimal maxPowerKw) {
            this.maxPowerKw = maxPowerKw;
            return this;
        }

        public Builder firmwareVersion(String firmwareVersion) {
            this.firmwareVersion = firmwareVersion;
            return this;
        }

        public Builder ocppVersion(String ocppVersion) {
            this.ocppVersion = ocppVersion;
            return this;
        }

        public Builder supportsSmartCharging(boolean supportsSmartCharging) {
            this.supportsSmartCharging = supportsSmartCharging;
            return this;
        }

        public Builder supportsReservation(boolean supportsReservation) {
            this.supportsReservation = supportsReservation;
            return this;
        }

        public Builder active(boolean active) {
            this.active = active;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public ChargerModel build() {
            return new ChargerModel(this);
        }
    }
}
