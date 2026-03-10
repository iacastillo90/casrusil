package com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.ocpp.dto.advanced;

/**
 * OCPP 1.6: ClearCache Response (Charger → Server).
 */
public record ClearCacheResponse(
        String status // "Accepted" | "Rejected"
) {
}
