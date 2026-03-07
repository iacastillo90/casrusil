package com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.ocpp.dto;

/**
 * OCPP 1.6: StartTransaction Response.
 */
public record StartTransactionResponse(
        IdTagInfo idTagInfo,
        int transactionId) {
}
