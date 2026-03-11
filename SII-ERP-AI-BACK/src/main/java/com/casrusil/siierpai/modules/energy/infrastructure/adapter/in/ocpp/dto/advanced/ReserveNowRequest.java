package com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.ocpp.dto.advanced;

/**
 * OCPP 1.6: ReserveNow Request (Server → Charger).
 * Reserva un conector para un usuario específico.
 */
public record ReserveNowRequest(
        int connectorId,
        String expiryDate,
        String idTag,
        int reservationId,
        String parentIdTag
) {}
