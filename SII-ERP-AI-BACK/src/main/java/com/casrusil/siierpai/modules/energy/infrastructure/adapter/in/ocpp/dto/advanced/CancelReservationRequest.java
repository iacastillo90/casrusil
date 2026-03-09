package com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.ocpp.dto.advanced;

/**
 * OCPP 1.6: CancelReservation Request (Server → Charger).
 */
public record CancelReservationRequest(
        int reservationId) {
}
