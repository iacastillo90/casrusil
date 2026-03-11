package com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.ocpp.dto.advanced;

/**
 * OCPP 1.6: TriggerMessage Request (Server → Charger).
 * Solicita al cargador que envíe un mensaje específico.
 */
public record TriggerMessageRequest(
        String requestedMessage, // "BootNotification" | "DiagnosticsStatusNotification" |
                                 // "FirmwareStatusNotification" | "Heartbeat" | "MeterValues" |
                                 // "StatusNotification"
        Integer connectorId) {
}
