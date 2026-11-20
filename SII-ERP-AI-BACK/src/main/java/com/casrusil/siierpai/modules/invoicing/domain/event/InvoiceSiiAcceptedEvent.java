package com.casrusil.siierpai.modules.invoicing.domain.event;

import com.casrusil.siierpai.modules.invoicing.domain.model.Invoice;

/**
 * El SII aceptó el DTE (ACEPTADO o ACEPTADO_CON_REPAROS).
 *
 * <p>
 * Dispara el asiento contable de la venta. Se publica una sola vez por factura
 * porque el servicio de estados es idempotente.
 * </p>
 *
 * @param invoice factura aceptada
 */
public record InvoiceSiiAcceptedEvent(Invoice invoice) {
}
