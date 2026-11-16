package com.casrusil.siierpai.modules.integration_sii.infrastructure.adapter.in.rest.dto;

import com.casrusil.siierpai.modules.invoicing.domain.model.Invoice;

import java.math.BigDecimal;

/**
 * Response body for a single DTE status lookup.
 *
 * @param tipo numeric SII document type code, as a string
 * @param folio document folio
 * @param rutEmisor issuer RUT
 * @param rutReceptor receiver RUT
 * @param estado submission state ({@code PENDIENTE} or {@code ENVIADO})
 * @param fechaEmision issue date in ISO-8601 format
 * @param monto total amount
 */
public record DteStatusDto(
        String tipo,
        Long folio,
        String rutEmisor,
        String rutReceptor,
        String estado,
        String fechaEmision,
        BigDecimal monto) {

    public static DteStatusDto from(Invoice invoice) {
        return new DteStatusDto(
                String.valueOf(invoice.getType().getCode()),
                invoice.getFolio(),
                invoice.getIssuerRut(),
                invoice.getReceiverRut(),
                invoice.getDteStatus() != null ? invoice.getDteStatus() : Invoice.DTE_STATUS_PENDING,
                invoice.getDate() != null ? invoice.getDate().toString() : null,
                invoice.getTotalAmount());
    }
}
