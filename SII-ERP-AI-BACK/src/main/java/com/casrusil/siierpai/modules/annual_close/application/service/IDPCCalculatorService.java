package com.casrusil.siierpai.modules.annual_close.application.service;

import com.casrusil.siierpai.modules.annual_close.domain.model.TaxRegime;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class IDPCCalculatorService {

    public BigDecimal calculateIDPC(BigDecimal rli, TaxRegime regime) {
        if (rli.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO; // No tax on loss
        }
        
        BigDecimal taxRate;
        switch (regime) {
            case REGIME_14A_SEMI_INTEGRADO:
                taxRate = new BigDecimal("0.27"); // 27%
                break;
            case REGIME_14D3_PROPYME_GENERAL:
                taxRate = new BigDecimal("0.25"); // 25% (Temporarily it might be 10% or 12.5% depending on the specific transition year, defaulting to standard 25%)
                break;
            case REGIME_14D8_PROPYME_TRANSPARENTE:
                taxRate = BigDecimal.ZERO; // Exempt from IDPC, owners pay global complementary tax
                break;
            default:
                taxRate = new BigDecimal("0.25");
        }
        
        return rli.multiply(taxRate);
    }
}
