package com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.ocpp.dto.command;

/**
 * OCPP 1.6: UnlockConnector Request (Server → Charger).
 * Comando para desbloquear físicamente un conector.
 */
public record UnlockConnectorRequest(
        int connectorId) {
}
