package com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.ocpp.dto.command;

/**
 * OCPP 1.6: Reset Response (Charger → Server).
 */
public record ResetResponse(
        String status // "Accepted" | "Rejected"
) {
}
