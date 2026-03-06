package com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.ocpp.dto;

import java.util.List;

/**
 * OCPP 1.6: MeterValue.
 * Conjunto de mediciones en un momento específico.
 */
public record MeterValue(
        String timestamp,
        List<SampledValue> sampledValue) {
}
