package com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.ocpp.dto;

/**
 * OCPP 1.6: IdTagInfo.
 * Información sobre la autorización de un idTag.
 */
public record IdTagInfo(
        String status, // "Accepted" | "Blocked" | "Expired" | "Invalid" | "ConcurrentTx"
        String expiryDate,
        String parentIdTag) {
}
