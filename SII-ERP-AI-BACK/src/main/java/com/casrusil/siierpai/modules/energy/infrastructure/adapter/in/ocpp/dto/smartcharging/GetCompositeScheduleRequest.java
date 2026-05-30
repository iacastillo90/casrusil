package com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.ocpp.dto.smartcharging;

/**
 * OCPP 1.6: GetCompositeSchedule Request (Server → Charger).
 * Obtiene el horario de carga calculado.
 */
public record GetCompositeScheduleRequest(
        int connectorId,
        int duration,
        String chargingRateUnit // "W" | "A"
) {
}
