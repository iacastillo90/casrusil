package com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.ocpp.dto.advanced;

/**
 * OCPP 1.6: DataTransfer Response (Bidireccional).
 */
public record DataTransferResponse(
        String status, // "Accepted" | "Rejected" | "UnknownMessageId" | "UnknownVendorId"
        String data) {
}
