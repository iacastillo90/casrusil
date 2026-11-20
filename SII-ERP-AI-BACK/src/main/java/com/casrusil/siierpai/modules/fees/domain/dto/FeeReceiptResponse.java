package com.casrusil.siierpai.modules.fees.domain.dto;

import com.casrusil.siierpai.modules.fees.domain.model.FeeReceipt;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Contrato de salida de {@code GET /api/v1/fees}.
 *
 * <p>
 * Expone únicamente los campos que consume el front de honorarios
 * ({@code features/fees/services/fee.service.ts}). El {@code companyId} no se
 * expone porque el aislamiento multi-tenant lo resuelve el backend desde el JWT.
 * </p>
 */
public record FeeReceiptResponse(
        UUID id,
        Long folio,
        String issuerRut,
        String issuerName,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd") LocalDate issueDate,
        BigDecimal grossAmount,
        BigDecimal retentionAmount,
        BigDecimal netAmount,
        String status) {

    public static FeeReceiptResponse from(FeeReceipt feeReceipt) {
        return new FeeReceiptResponse(
                feeReceipt.getId(),
                feeReceipt.getFolio(),
                feeReceipt.getIssuerRut(),
                feeReceipt.getIssuerName(),
                feeReceipt.getIssueDate(),
                feeReceipt.getGrossAmount(),
                feeReceipt.getRetentionAmount(),
                feeReceipt.getNetAmount(),
                feeReceipt.getStatus() != null ? feeReceipt.getStatus().name() : null);
    }
}
