package com.casrusil.siierpai.modules.payroll.domain.model;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.YearMonth;

@Data
@Builder
public class PayrollPeriod {
    private Long id;
    private String companyId;
    private YearMonth period; // e.g. 2026-11
    private BigDecimal ufValue; // Unidad de Fomento
    private BigDecimal utmValue; // Unidad Tributaria Mensual
    private boolean isClosed;
}
