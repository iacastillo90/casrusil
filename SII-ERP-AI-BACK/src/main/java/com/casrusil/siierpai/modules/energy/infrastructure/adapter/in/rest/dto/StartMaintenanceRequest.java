package com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for starting maintenance.
 */
public record StartMaintenanceRequest(
        @NotBlank(message = "Technician name is required") String technician) {
}
