package com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.rest.dto;

import com.casrusil.siierpai.modules.energy.application.service.maintenance.MaintenanceService.MaintenanceSummary;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for maintenance summary statistics.
 */
public record MaintenanceSummaryDto(
        UUID stationId,
        long totalMaintenanceCount,
        long completedMaintenanceCount,
        BigDecimal totalCost,
        LocalDateTime lastMaintenanceDate,
        double completionRate) {
    /**
     * Converts domain summary to DTO.
     */
    public static MaintenanceSummaryDto from(MaintenanceSummary summary) {
        double completionRate = summary.totalMaintenanceCount() > 0
                ? (double) summary.completedMaintenanceCount() / summary.totalMaintenanceCount() * 100
                : 0.0;

        return new MaintenanceSummaryDto(
                summary.stationId().getValue(),
                summary.totalMaintenanceCount(),
                summary.completedMaintenanceCount(),
                summary.totalCost(),
                summary.lastMaintenanceDate(),
                completionRate);
    }
}
