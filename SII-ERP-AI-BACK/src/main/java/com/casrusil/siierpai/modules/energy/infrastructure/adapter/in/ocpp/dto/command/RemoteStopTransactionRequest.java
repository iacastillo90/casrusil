package com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.ocpp.dto.command;

/**
 * OCPP 1.6: RemoteStopTransaction Request (Server → Charger).
 * Comando para detener una transacción de carga remotamente.
 */
public record RemoteStopTransactionRequest(
        int transactionId) {
}
