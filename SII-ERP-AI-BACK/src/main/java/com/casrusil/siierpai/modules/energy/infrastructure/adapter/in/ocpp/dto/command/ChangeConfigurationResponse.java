package com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.ocpp.dto.command;

/**
 * OCPP 1.6: ChangeConfiguration Response (Charger → Server).
 */
public record ChangeConfigurationResponse(
        String status // "Accepted" | "Rejected" | "RebootRequired" | "NotSupported"
) {
}
