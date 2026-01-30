package com.casrusil.siierpai.modules.annual_close.application.service;

import com.casrusil.siierpai.modules.annual_close.domain.model.TaxAdjustment;
import com.casrusil.siierpai.modules.annual_close.domain.port.TaxAdjustmentRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RLICalculatorService {

    private final TaxAdjustmentRepositoryPort taxAdjustmentRepository;

    public BigDecimal calculateRLI(BigDecimal financialResult, String companyId, int fiscalYear) {
        List<TaxAdjustment> adjustments = taxAdjustmentRepository.findByCompanyIdAndFiscalYear(companyId, fiscalYear);
        
        BigDecimal rli = financialResult;
        
        for (TaxAdjustment adj : adjustments) {
            if ("ADDITION".equals(adj.getType())) {
                rli = rli.add(adj.getAmount());
            } else if ("DEDUCTION".equals(adj.getType())) {
                rli = rli.subtract(adj.getAmount());
            }
        }
        
        return rli;
    }
    
    public BigDecimal calculateTotalAdditions(String companyId, int fiscalYear) {
        return taxAdjustmentRepository.findByCompanyIdAndFiscalYear(companyId, fiscalYear).stream()
                .filter(a -> "ADDITION".equals(a.getType()))
                .map(TaxAdjustment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    public BigDecimal calculateTotalDeductions(String companyId, int fiscalYear) {
        return taxAdjustmentRepository.findByCompanyIdAndFiscalYear(companyId, fiscalYear).stream()
                .filter(a -> "DEDUCTION".equals(a.getType()))
                .map(TaxAdjustment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
