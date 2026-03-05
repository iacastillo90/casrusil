package com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.ocpp.dto;

/**
 * OCPP 1.6: BootNotification Response.
 * Respuesta del servidor al cargador.
 */
public record BootNotificationResponse(
        String status, // "Accepted" | "Pending" | "Rejected"
        String currentTime, // ISO 8601
        int interval // Heartbeat interval en segundos
) {
}
