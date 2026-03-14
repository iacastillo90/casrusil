package com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.ocpp.dto.command;

/**
 * OCPP 1.6: RemoteStartTransaction Response (Charger → Server).
 */
public record RemoteStartTransactionResponse(
        String status // "Accepted" | "Rejected"
) {
}
