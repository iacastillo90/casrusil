package com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.ocpp.dto;

/**
 * OCPP 1.6: Heartbeat Response.
 */
public record HeartbeatResponse(
        String currentTime // ISO 8601
) {
}
