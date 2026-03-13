package com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.ocpp.dto.command;

import java.util.List;

/**
 * OCPP 1.6: GetConfiguration Response (Charger → Server).
 */
public record GetConfigurationResponse(
        List<KeyValue> configurationKey,
        List<String> unknownKey) {
}
