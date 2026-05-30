package com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.ocpp.dto.smartcharging;

/**
 * OCPP 1.6: SetChargingProfile Response (Charger → Server).
 */
public record SetChargingProfileResponse(
        String status // "Accepted" | "Rejected" | "NotSupported"
) {
}
