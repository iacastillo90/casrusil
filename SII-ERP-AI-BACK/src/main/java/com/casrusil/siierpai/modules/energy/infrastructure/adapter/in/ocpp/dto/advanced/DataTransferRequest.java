package com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.ocpp.dto.advanced;

/**
 * OCPP 1.6: DataTransfer Request (Bidireccional).
 * Permite transferencia de datos vendor-specific.
 */
public record DataTransferRequest(
        String vendorId,
        String messageId,
        String data) {
}
