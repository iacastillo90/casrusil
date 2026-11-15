package com.casrusil.siierpai.modules.payroll.domain.model;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class Payslip {
    private Long id;
    private String companyId;
    private UUID employeeId;
    private Long periodId;
    
    // Haberes
    private BigDecimal baseSalary;
    private BigDecimal gratification;
    private BigDecimal taxableIncome; // Total Imponible
    private BigDecimal nonTaxableIncome; // Movilización, Colación, etc.
    
    // Descuentos Legales
    private BigDecimal afpDiscount;
    private BigDecimal healthDiscount;
    private BigDecimal unemploymentInsurance; // AFC
    private BigDecimal singleTax; // Impuesto Único 2da Categoría
    
    // Totales
    private BigDecimal totalDiscounts;
    private BigDecimal netSalary; // Sueldo Líquido a pagar
}
