package com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.ocpp.dto.advanced;

/**
 * OCPP 1.6: Authorize Request (Charger → Server).
 * Valida un idTag (RFID) antes de iniciar carga.
 */
public record AuthorizeRequest(
        String idTag) {
}
