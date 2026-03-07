package com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.ocpp.dto;

/**
 * OCPP 1.6: StartTransaction Request.
 * Enviado por el cargador al iniciar una transacción de carga.
 */
public record StartTransactionRequest(
        int connectorId,
        String idTag,
        int meterStart,
        String timestamp,
        Integer reservationId
) {}
