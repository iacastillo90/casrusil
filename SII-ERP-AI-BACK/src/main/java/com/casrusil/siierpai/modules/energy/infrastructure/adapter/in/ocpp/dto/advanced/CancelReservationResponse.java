package com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.ocpp.dto.advanced;

/**
 * OCPP 1.6: CancelReservation Response (Charger → Server).
 */
public record CancelReservationResponse(
        String status // "Accepted" | "Rejected"
) {
}
