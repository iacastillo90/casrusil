package com.casrusil.siierpai.modules.energy.domain.event;

import com.casrusil.siierpai.modules.energy.domain.model.StationId;
import com.casrusil.siierpai.modules.energy.domain.model.maintenance.MaintenanceRecordId;
import com.casrusil.siierpai.modules.energy.domain.model.maintenance.MaintenanceType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Domain events for maintenance operations.
 * Uses sealed interface for type safety and exhaustive pattern matching.
 */
public sealed interface MaintenanceEvent {

    /**
     * Event published when maintenance is scheduled.
     */
    record MaintenanceScheduled(
            MaintenanceRecordId recordId,
            StationId stationId,
            MaintenanceType type,
            LocalDateTime scheduledDate,
            String description) implements MaintenanceEvent {
    }

    /**
     * Event published when maintenance work starts.
     */
    record MaintenanceStarted(
            MaintenanceRecordId recordId,
            StationId stationId,
            String technician,
            LocalDateTime startedAt) implements MaintenanceEvent {
    }

    /**
     * Event published when maintenance is successfully completed.
     */
    record MaintenanceCompleted(
            MaintenanceRecordId recordId,
            StationId stationId,
            BigDecimal cost,
            String notes,
            LocalDateTime completedAt) implements MaintenanceEvent {
    }

    /**
     * Event published when maintenance fails.
     */
    record MaintenanceFailed(
            MaintenanceRecordId recordId,
            StationId stationId,
            String reason,
            LocalDateTime failedAt) implements MaintenanceEvent {
    }

    /**
     * Event published when maintenance is cancelled.
     */
    record MaintenanceCancelled(
            MaintenanceRecordId recordId,
            StationId stationId,
            String reason,
            LocalDateTime cancelledAt) implements MaintenanceEvent {
    }

    /**
     * Event published when a scheduled maintenance becomes overdue.
     */
    record MaintenanceOverdue(
            MaintenanceRecordId recordId,
            StationId stationId,
            MaintenanceType type,
            LocalDateTime scheduledDate,
            LocalDateTime detectedAt) implements MaintenanceEvent {
    }
}
