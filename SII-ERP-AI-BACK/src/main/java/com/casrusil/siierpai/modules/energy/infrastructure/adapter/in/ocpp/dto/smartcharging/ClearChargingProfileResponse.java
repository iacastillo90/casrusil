package com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.ocpp.dto.smartcharging;

/**
 * OCPP 1.6: ClearChargingProfile Response (Charger → Server).
 */
public record ClearChargingProfileResponse(
        String status // "Accepted" | "Unknown"
) {
}
