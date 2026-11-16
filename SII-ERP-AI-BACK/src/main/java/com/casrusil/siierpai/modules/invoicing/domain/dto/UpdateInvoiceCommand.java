package com.casrusil.siierpai.modules.invoicing.domain.dto;

import java.math.BigDecimal;
import java.util.List;

public record UpdateInvoiceCommand(
        String type,
        String clientRut,
        List<UpdateInvoiceLineCommand> lines,
        BigDecimal netAmount,
        BigDecimal taxAmount,
        BigDecimal totalAmount,
        String issueDate,
        Long folio,
        String status,
        String issuerRut,
        String recipientName) {

    public record UpdateInvoiceLineCommand(
            String description,
            BigDecimal quantity,
            BigDecimal unitPrice,
            BigDecimal lineTotal) {
    }
}
