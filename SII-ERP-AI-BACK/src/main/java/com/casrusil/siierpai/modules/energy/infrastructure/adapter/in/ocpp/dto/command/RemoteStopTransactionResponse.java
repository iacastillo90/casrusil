package com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.ocpp.dto.command;

/**
 * OCPP 1.6: RemoteStopTransaction Response (Charger → Server).
 */
public record RemoteStopTransactionResponse(
        String status // "Accepted" | "Rejected"
) {
}
