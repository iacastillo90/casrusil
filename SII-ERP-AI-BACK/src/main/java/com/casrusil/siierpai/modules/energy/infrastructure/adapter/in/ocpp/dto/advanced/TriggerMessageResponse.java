package com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.ocpp.dto.advanced;

/**
 * OCPP 1.6: TriggerMessage Response (Charger → Server).
 */
public record TriggerMessageResponse(
        String status // "Accepted" | "Rejected" | "NotImplemented"
) {
}
