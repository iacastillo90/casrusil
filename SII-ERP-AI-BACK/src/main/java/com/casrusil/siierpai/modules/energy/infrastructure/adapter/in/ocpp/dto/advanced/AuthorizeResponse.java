package com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.ocpp.dto.advanced;

import com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.ocpp.dto.IdTagInfo;

/**
 * OCPP 1.6: Authorize Response (Server → Charger).
 */
public record AuthorizeResponse(
        IdTagInfo idTagInfo) {
}
