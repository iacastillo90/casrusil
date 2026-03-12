package com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.ocpp.dto.command;

/**
 * OCPP 1.6: ChangeAvailability Request (Server → Charger).
 * Cambia la disponibilidad de un conector.
 */
public record ChangeAvailabilityRequest(
        int connectorId, // 0 = toda la estación
        String type // "Operative" | "Inoperative"
) {
}
