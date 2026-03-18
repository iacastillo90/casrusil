package com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.ocpp.dto.localauth;

import java.time.LocalDateTime;

/**
 * Authorization data for local auth list.
 * Contains idTag and authorization status.
 */
public record AuthorizationData(
        String idTag,
        IdTagInfo idTagInfo) {
    public record IdTagInfo(
            String status, // "Accepted", "Blocked", "Expired", "Invalid", "ConcurrentTx"
            LocalDateTime expiryDate,
            String parentIdTag) {
    }
}
