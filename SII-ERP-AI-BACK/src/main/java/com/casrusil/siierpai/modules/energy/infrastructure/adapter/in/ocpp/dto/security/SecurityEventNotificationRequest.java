package com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.ocpp.dto.security;

/**
 * OCPP 1.6 Security Extension: SecurityEventNotification.req
 */
public record SecurityEventNotificationRequest(
        String type, // FirmwareUpdated, FailedToAuthenticateAtCsms, CsmsFailedToAuthenticate, etc.
        String timestamp, // ISO 8601 format
        String techInfo // Optional technical information
) {
}
