package com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.ocpp.dto;

import java.util.List;

/**
 * OCPP 1.6: MeterValues Request.
 * Enviado por el cargador con mediciones periódicas de energía.
 */
public record MeterValuesRequest(
        int connectorId,
        Integer transactionId,
        List<MeterValue> meterValue) {
}
