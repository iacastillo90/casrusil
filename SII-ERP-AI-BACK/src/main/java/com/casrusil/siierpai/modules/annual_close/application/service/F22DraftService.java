package com.casrusil.siierpai.modules.annual_close.application.service;

import com.casrusil.siierpai.modules.annual_close.domain.dto.AnnualCloseResponse;
import com.casrusil.siierpai.modules.annual_close.domain.model.CompanyTaxProfile;
import com.casrusil.siierpai.modules.annual_close.domain.port.CompanyTaxProfileRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class F22DraftService implements AnnualCloseUseCase {

    private final CompanyTaxProfileRepositoryPort taxProfileRepository;
    private final RLICalculatorService rliCalculatorService;
    private final IDPCCalculatorService idpcCalculatorService;

    @Override
    public AnnualCloseResponse calculateAnnualTaxes(int fiscalYear, String companyId) {
        CompanyTaxProfile profile = taxProfileRepository.findByCompanyId(companyId)
                .orElseThrow(() -> new RuntimeException("Tax profile not found for company"));
                
        // Mock financial result logic (Normally comes from Accounting Module)
        BigDecimal simulatedFinancialResult = new BigDecimal("10000000"); // 10 million base profit
        
        BigDecimal totalAdditions = rliCalculatorService.calculateTotalAdditions(companyId, fiscalYear);
        BigDecimal totalDeductions = rliCalculatorService.calculateTotalDeductions(companyId, fiscalYear);
        
        BigDecimal rli = rliCalculatorService.calculateRLI(simulatedFinancialResult, companyId, fiscalYear);
        
        BigDecimal idpc = idpcCalculatorService.calculateIDPC(rli, profile.getRegime());
        
        // Mock PPM logic
        BigDecimal ppmTotal = new BigDecimal("500000"); 
        
        BigDecimal taxToPayOrReturn = idpc.subtract(ppmTotal);
        
        return AnnualCloseResponse.builder()
                .companyId(companyId)
                .fiscalYear(fiscalYear)
                .financialResult(simulatedFinancialResult)
                .totalAdditions(totalAdditions)
                .totalDeductions(totalDeductions)
                .rli(rli)
                .calculatedTax(idpc)
                .totalPPM(ppmTotal)
                .taxToPayOrReturn(taxToPayOrReturn)
                .build();
    }
}
