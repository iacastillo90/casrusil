package com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.ocpp.dto.advanced;

/**
 * OCPP 1.6: ReserveNow Response (Charger → Server).
 */
public record ReserveNowResponse(
        String status // "Accepted" | "Faulted" | "Occupied" | "Rejected" | "Unavailable"
) {
}
