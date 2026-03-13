package com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.ocpp.dto.command;

/**
 * OCPP 1.6: ChangeConfiguration Request (Server → Charger).
 * Cambia un parámetro de configuración del cargador.
 */
public record ChangeConfigurationRequest(
        String key, // Nombre del parámetro (ej: "HeartbeatInterval", "MeterValueSampleInterval")
        String value // Nuevo valor
) {
}
