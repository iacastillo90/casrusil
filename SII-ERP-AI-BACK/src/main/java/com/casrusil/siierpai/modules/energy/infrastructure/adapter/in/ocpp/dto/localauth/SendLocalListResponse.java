package com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.ocpp.dto.localauth;

/**
 * OCPP 1.6: SendLocalList.conf
 * Response to SendLocalList request.
 */
public record SendLocalListResponse(
        String status // "Accepted", "Failed", "NotSupported", "VersionMismatch"
) {
}
