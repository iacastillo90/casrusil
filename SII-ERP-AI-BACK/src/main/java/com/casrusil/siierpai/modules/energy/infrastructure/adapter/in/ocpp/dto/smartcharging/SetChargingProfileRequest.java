package com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.ocpp.dto.smartcharging;

/**
 * OCPP 1.6: SetChargingProfile Request (Server → Charger).
 * Establece un perfil de carga para optimización de energía.
 */
public record SetChargingProfileRequest(
        int connectorId,
        ChargingProfile csChargingProfiles) {
}
