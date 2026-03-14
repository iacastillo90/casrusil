package com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.ocpp.dto.command;

/**
 * OCPP 1.6: KeyValue.
 * Par clave-valor de configuración.
 */
public record KeyValue(
        String key,
        boolean readonly,
        String value) {
}
