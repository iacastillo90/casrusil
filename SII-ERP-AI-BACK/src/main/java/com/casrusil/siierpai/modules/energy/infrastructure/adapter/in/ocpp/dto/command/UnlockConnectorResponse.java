package com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.ocpp.dto.command;

/**
 * OCPP 1.6: UnlockConnector Response (Charger → Server).
 */
public record UnlockConnectorResponse(
        String status // "Unlocked" | "UnlockFailed" | "NotSupported"
) {
}
