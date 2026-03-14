package com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.ocpp.dto.command;

/**
 * OCPP 1.6: RemoteStartTransaction Request (Server → Charger).
 * Comando para iniciar una transacción de carga remotamente.
 */
public record RemoteStartTransactionRequest(
        int connectorId,
        String idTag
// ChargingProfile chargingProfile - Opcional, para Smart Charging
) {
}
