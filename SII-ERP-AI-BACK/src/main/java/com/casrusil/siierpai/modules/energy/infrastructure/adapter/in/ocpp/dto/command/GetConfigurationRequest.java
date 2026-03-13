package com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.ocpp.dto.command;

import java.util.List;

/**
 * OCPP 1.6: GetConfiguration Request (Server → Charger).
 * Obtiene la configuración actual del cargador.
 */
public record GetConfigurationRequest(
        List<String> key // Lista de claves a obtener (vacío = todas)
) {
}
