package com.casrusil.siierpai.modules.payroll.domain.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class PayslipResponse {
    private Long id;
    private String employeeName;
    private String rut;
    private String period; // YYYY-MM
    private BigDecimal taxableIncome;
    private BigDecimal nonTaxableIncome;
    private BigDecimal totalDiscounts;
    private BigDecimal netSalary;
}
