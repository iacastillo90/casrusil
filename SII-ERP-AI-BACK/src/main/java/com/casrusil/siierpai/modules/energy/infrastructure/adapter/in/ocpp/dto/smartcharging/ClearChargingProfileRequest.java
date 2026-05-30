package com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.ocpp.dto.smartcharging;

/**
 * OCPP 1.6: ClearChargingProfile Request (Server → Charger).
 * Elimina perfiles de carga.
 */
public record ClearChargingProfileRequest(
        Integer id,
        Integer connectorId,
        String chargingProfilePurpose,
        Integer stackLevel) {
}
