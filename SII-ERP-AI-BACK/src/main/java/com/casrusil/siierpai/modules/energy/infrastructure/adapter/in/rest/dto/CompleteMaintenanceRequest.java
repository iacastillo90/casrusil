package com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

/**
 * Request DTO for completing maintenance.
 */
public record CompleteMaintenanceRequest(
        @NotBlank(message = "Completion notes are required") String notes,

        @PositiveOrZero(message = "Cost must be zero or positive") BigDecimal cost) {
}
