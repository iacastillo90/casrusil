package com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.rest.dto;

import com.casrusil.siierpai.modules.energy.domain.model.maintenance.MaintenanceType;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Request DTO for scheduling maintenance.
 */
public record ScheduleMaintenanceRequest(
        @NotNull(message = "Station ID is required") UUID stationId,

        @NotNull(message = "Maintenance type is required") MaintenanceType type,

        @NotNull(message = "Scheduled date is required") @Future(message = "Scheduled date must be in the future") LocalDateTime scheduledDate,

        @NotBlank(message = "Description is required") String description) {
}
