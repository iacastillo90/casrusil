package com.casrusil.siierpai.modules.annual_close.domain.model;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class TaxAdjustment {
    private Long id;
    private String companyId;
    private int fiscalYear;
    private String type; // ADDITION (Agregado), DEDUCTION (Deducción)
    private String description;
    private BigDecimal amount;
}
