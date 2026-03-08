package com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.ocpp.dto;

import java.util.List;

/**
 * OCPP 1.6: StopTransaction Request.
 * Enviado por el cargador al finalizar una transacción de carga.
 */
public record StopTransactionRequest(
        int transactionId,
        String idTag,
        int meterStop,
        String timestamp,
        String reason, // "EmergencyStop" | "EVDisconnected" | "HardReset" | "Local" | "Other" |
                       // "PowerLoss" | "Reboot" | "Remote" | "SoftReset" | "UnlockCommand" |
                       // "DeAuthorized"
        List<MeterValue> transactionData) {
}
