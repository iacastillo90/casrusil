package com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.ocpp.dto.smartcharging;

/**
 * OCPP 1.6: GetCompositeSchedule Response (Charger → Server).
 */
public record GetCompositeScheduleResponse(
        String status, // "Accepted" | "Rejected"
        Integer connectorId,
        String scheduleStart,
        ChargingSchedule chargingSchedule) {
}
