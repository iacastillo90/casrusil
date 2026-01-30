package com.casrusil.siierpai.modules.annual_close.domain.model;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class CompanyTaxProfile {
    private String companyId;
    private TaxRegime regime;
    private BigDecimal ppmRate; // Pagos Provisionales Mensuales rate
}
