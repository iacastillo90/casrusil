package com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.ocpp.dto.localauth;

/**
 * OCPP 1.6: GetLocalListVersion.conf
 * Returns the version number of the local authorization list.
 */
public record GetLocalListVersionResponse(
        int listVersion // -1 if no list is configured
) {
}
