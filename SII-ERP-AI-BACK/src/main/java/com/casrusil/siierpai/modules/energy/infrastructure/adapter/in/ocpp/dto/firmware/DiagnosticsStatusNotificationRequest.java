package com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.ocpp.dto.firmware;

/**
 * OCPP 1.6: DiagnosticsStatusNotification Request (Charger → Server).
 * Notifica el progreso de la generación de diagnósticos.
 */
public record DiagnosticsStatusNotificationRequest(
        String status // "Idle" | "Uploaded" | "UploadFailed" | "Uploading"
) {
}
