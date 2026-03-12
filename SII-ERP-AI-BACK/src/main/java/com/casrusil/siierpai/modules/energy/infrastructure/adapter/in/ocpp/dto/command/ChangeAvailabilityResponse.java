package com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.ocpp.dto.command;

/**
 * OCPP 1.6: ChangeAvailability Response (Charger → Server).
 */
public record ChangeAvailabilityResponse(
        String status // "Accepted" | "Rejected" | "Scheduled"
) {
}
