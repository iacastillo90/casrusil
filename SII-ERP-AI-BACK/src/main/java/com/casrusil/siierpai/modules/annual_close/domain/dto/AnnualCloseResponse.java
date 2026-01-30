package com.casrusil.siierpai.modules.annual_close.domain.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class AnnualCloseResponse {
    private String companyId;
    private int fiscalYear;
    private BigDecimal financialResult; // Utilidad/Pérdida Financiera
    private BigDecimal totalAdditions; // Agregados (Gastos Rechazados)
    private BigDecimal totalDeductions; // Deducciones
    private BigDecimal rli; // Renta Líquida Imponible
    private BigDecimal calculatedTax; // Impuesto de Primera Categoría (IDPC)
    private BigDecimal totalPPM; // Pagos Provisionales Mensuales
    private BigDecimal taxToPayOrReturn; // A pagar o devolver
}
