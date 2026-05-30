package com.casrusil.siierpai.modules.payroll.domain.model;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class Employee {
    private Long id;
    private String companyId;
    private String rut;
    private String fullName;
    private BigDecimal baseSalary;
    private String afp; // Habitat, Provida, etc.
    private String healthSystem; // Fonasa, Isapre
    private BigDecimal healthPercentage; // e.g., 7% default or UF agreed for Isapre
    private Integer familyLoads;
}
