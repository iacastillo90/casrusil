package com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.ocpp.dto.firmware;

/**
 * OCPP 1.6: UpdateFirmware Request (Server → Charger).
 * Solicita actualización de firmware OTA.
 */
public record UpdateFirmwareRequest(
        String location, // URL del archivo de firmware
        String retrieveDate, // Fecha/hora para descargar el firmware
        Integer retries,
        Integer retryInterval) {
}
