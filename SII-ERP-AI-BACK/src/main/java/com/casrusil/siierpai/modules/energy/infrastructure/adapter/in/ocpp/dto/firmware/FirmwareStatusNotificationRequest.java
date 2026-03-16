package com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.ocpp.dto.firmware;

/**
 * OCPP 1.6: FirmwareStatusNotification Request (Charger → Server).
 * Notifica el progreso de la actualización de firmware.
 */
public record FirmwareStatusNotificationRequest(
        String status // "Downloaded" | "DownloadFailed" | "Downloading" | "Idle" |
                      // "InstallationFailed" | "Installing" | "Installed"
) {
}
