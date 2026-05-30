package com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.rest.dto;

import com.casrusil.siierpai.modules.energy.domain.model.maintenance.MaintenanceRecord;
import com.casrusil.siierpai.modules.energy.domain.model.maintenance.MaintenanceStatus;
import com.casrusil.siierpai.modules.energy.domain.model.maintenance.MaintenanceType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for MaintenanceRecord API responses.
 */
public record MaintenanceRecordDto(
        UUID id,
        UUID stationId,
        MaintenanceType type,
        MaintenanceStatus status,
        LocalDateTime scheduledDate,
        LocalDateTime startedAt,
        LocalDateTime completedAt,
        String description,
        String technician,
        String notes,
        BigDecimal cost,
        boolean overdue,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
    /**
     * Converts domain model to DTO.
     */
    public static MaintenanceRecordDto from(MaintenanceRecord record) {
        return new MaintenanceRecordDto(
                record.getId().getValue(),
                record.getStationId().getValue(),
                record.getType(),
                record.getStatus(),
                record.getScheduledDate(),
                record.getStartedAt(),
                record.getCompletedAt(),
                record.getDescription(),
                record.getTechnician(),
                record.getNotes(),
                record.getCost(),
                record.isOverdue(),
                record.getCreatedAt(),
                record.getUpdatedAt());
    }
}
