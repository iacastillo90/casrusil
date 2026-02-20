package com.casrusil.siierpai.modules.energy.domain.model.maintenance;

import com.casrusil.siierpai.modules.energy.domain.model.StationId;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Maintenance Record aggregate root.
 * 
 * Tracks maintenance activities for charging stations.
 */
public class MaintenanceRecord {
    private final MaintenanceRecordId id;
    private final StationId stationId;
    private final MaintenanceType type;
    private MaintenanceStatus status;
    private final LocalDateTime scheduledDate;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private String description;
    private String technician;
    private String notes;
    private BigDecimal cost;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private MaintenanceRecord(Builder builder) {
        this.id = Objects.requireNonNull(builder.id, "Maintenance record ID is required");
        this.stationId = Objects.requireNonNull(builder.stationId, "Station ID is required");
        this.type = Objects.requireNonNull(builder.type, "Maintenance type is required");
        this.status = builder.status != null ? builder.status : MaintenanceStatus.SCHEDULED;
        this.scheduledDate = Objects.requireNonNull(builder.scheduledDate, "Scheduled date is required");
        this.startedAt = builder.startedAt;
        this.completedAt = builder.completedAt;
        this.description = Objects.requireNonNull(builder.description, "Description is required");
        this.technician = builder.technician;
        this.notes = builder.notes;
        this.cost = builder.cost;
        this.createdAt = Objects.requireNonNull(builder.createdAt, "Created timestamp is required");
        this.updatedAt = builder.updatedAt;
    }

    /**
     * Creates a new maintenance record.
     */
    public static MaintenanceRecord schedule(
            StationId stationId,
            MaintenanceType type,
            LocalDateTime scheduledDate,
            String description) {

        return builder()
                .id(MaintenanceRecordId.generate())
                .stationId(stationId)
                .type(type)
                .scheduledDate(scheduledDate)
                .description(description)
                .status(MaintenanceStatus.SCHEDULED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Starts the maintenance work.
     */
    public void start(String technician) {
        if (this.status != MaintenanceStatus.SCHEDULED) {
            throw new IllegalStateException("Can only start scheduled maintenance");
        }

        this.status = MaintenanceStatus.IN_PROGRESS;
        this.startedAt = LocalDateTime.now();
        this.technician = technician;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Completes the maintenance work.
     */
    public void complete(String notes, BigDecimal cost) {
        if (this.status != MaintenanceStatus.IN_PROGRESS) {
            throw new IllegalStateException("Can only complete in-progress maintenance");
        }

        this.status = MaintenanceStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
        this.notes = notes;
        this.cost = cost;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Marks the maintenance as failed.
     */
    public void fail(String notes) {
        if (this.status != MaintenanceStatus.IN_PROGRESS) {
            throw new IllegalStateException("Can only fail in-progress maintenance");
        }

        this.status = MaintenanceStatus.FAILED;
        this.completedAt = LocalDateTime.now();
        this.notes = notes;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Cancels the maintenance.
     */
    public void cancel(String reason) {
        if (this.status == MaintenanceStatus.COMPLETED || this.status == MaintenanceStatus.FAILED) {
            throw new IllegalStateException("Cannot cancel completed or failed maintenance");
        }

        this.status = MaintenanceStatus.CANCELLED;
        this.notes = reason;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Updates maintenance notes.
     */
    public void updateNotes(String notes) {
        this.notes = notes;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Checks if maintenance is overdue.
     */
    public boolean isOverdue() {
        return status == MaintenanceStatus.SCHEDULED
                && scheduledDate.isBefore(LocalDateTime.now());
    }

    // Getters
    public MaintenanceRecordId getId() {
        return id;
    }

    public StationId getStationId() {
        return stationId;
    }

    public MaintenanceType getType() {
        return type;
    }

    public MaintenanceStatus getStatus() {
        return status;
    }

    public LocalDateTime getScheduledDate() {
        return scheduledDate;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public String getDescription() {
        return description;
    }

    public String getTechnician() {
        return technician;
    }

    public String getNotes() {
        return notes;
    }

    public BigDecimal getCost() {
        return cost;
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
        private MaintenanceRecordId id;
        private StationId stationId;
        private MaintenanceType type;
        private MaintenanceStatus status;
        private LocalDateTime scheduledDate;
        private LocalDateTime startedAt;
        private LocalDateTime completedAt;
        private String description;
        private String technician;
        private String notes;
        private BigDecimal cost;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder id(MaintenanceRecordId id) {
            this.id = id;
            return this;
        }

        public Builder stationId(StationId stationId) {
            this.stationId = stationId;
            return this;
        }

        public Builder type(MaintenanceType type) {
            this.type = type;
            return this;
        }

        public Builder status(MaintenanceStatus status) {
            this.status = status;
            return this;
        }

        public Builder scheduledDate(LocalDateTime scheduledDate) {
            this.scheduledDate = scheduledDate;
            return this;
        }

        public Builder startedAt(LocalDateTime startedAt) {
            this.startedAt = startedAt;
            return this;
        }

        public Builder completedAt(LocalDateTime completedAt) {
            this.completedAt = completedAt;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder technician(String technician) {
            this.technician = technician;
            return this;
        }

        public Builder notes(String notes) {
            this.notes = notes;
            return this;
        }

        public Builder cost(BigDecimal cost) {
            this.cost = cost;
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

        public MaintenanceRecord build() {
            return new MaintenanceRecord(this);
        }
    }
}
