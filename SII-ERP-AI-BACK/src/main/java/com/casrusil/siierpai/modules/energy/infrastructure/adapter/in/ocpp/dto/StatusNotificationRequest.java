package com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.ocpp.dto;

/**
 * OCPP 1.6: StatusNotification Request.
 * Enviado por el cargador cuando cambia el estado de un conector.
 */
public record StatusNotificationRequest(
        int connectorId,
        String status, // "Available" | "Preparing" | "Charging" | "SuspendedEVSE" | "SuspendedEV" |
                       // "Finishing" | "Reserved" | "Unavailable" | "Faulted"
        String errorCode, // "NoError" | "ConnectorLockFailure" | "EVCommunicationError" | ...
        String info,
        String timestamp,
        String vendorId,
        String vendorErrorCode) {
}
