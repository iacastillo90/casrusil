package com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.ocpp.dto.firmware;

/**
 * OCPP 1.6: GetDiagnostics Response (Charger → Server).
 */
public record GetDiagnosticsResponse(
        String fileName // Nombre del archivo de diagnóstico (opcional)
) {
}
