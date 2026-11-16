package com.casrusil.siierpai.modules.invoicing.infrastructure.adapter.in.rest.dto;

import com.casrusil.siierpai.modules.invoicing.domain.dto.UpdateInvoiceCommand;
import com.casrusil.siierpai.modules.invoicing.domain.dto.UpdateInvoiceCommand.UpdateInvoiceLineCommand;

import java.math.BigDecimal;
import java.util.List;

public record UpdateInvoiceRequest(
        String type,
        String clientRut,
        List<Line> lines,
        BigDecimal netAmount,
        BigDecimal taxAmount,
        BigDecimal totalAmount,
        String issueDate,
        Long folio,
        String status,
        String issuerRut,
        String recipientName) {

    public UpdateInvoiceCommand toCommand() {
        List<UpdateInvoiceLineCommand> commandLines = lines == null
                ? null
                : lines.stream()
                        .map(line -> line == null
                                ? null
                                : new UpdateInvoiceLineCommand(line.description(), line.quantity(), line.unitPrice(),
                                        line.lineTotal()))
                        .toList();
        return new UpdateInvoiceCommand(type, clientRut, commandLines, netAmount, taxAmount, totalAmount, issueDate,
                folio, status, issuerRut, recipientName);
    }

    public record Line(
            String description,
            BigDecimal quantity,
            BigDecimal unitPrice,
            BigDecimal lineTotal) {
    }
}
