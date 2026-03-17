package com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.ocpp.dto.firmware;

/**
 * OCPP 1.6: GetDiagnostics Request (Server → Charger).
 * Solicita logs de diagnóstico del cargador.
 */
public record GetDiagnosticsRequest(
        String location, // URL donde subir el archivo de diagnóstico
        Integer retries,
        Integer retryInterval,
        String startTime, // Inicio del período de logs
        String stopTime // Fin del período de logs
) {
}
