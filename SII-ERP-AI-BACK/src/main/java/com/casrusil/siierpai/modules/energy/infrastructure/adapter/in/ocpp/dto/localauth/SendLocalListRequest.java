package com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.ocpp.dto.localauth;

import java.util.List;

/**
 * OCPP 1.6: SendLocalList.req
 * Sends a local authorization list to the charge point.
 */
public record SendLocalListRequest(
        int listVersion,
        List<AuthorizationData> localAuthorizationList,
        String updateType // "Full" or "Differential"
) {
}
