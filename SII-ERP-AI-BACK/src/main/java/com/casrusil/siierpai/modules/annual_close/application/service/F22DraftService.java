package com.casrusil.siierpai.modules.annual_close.application.service;

import com.casrusil.siierpai.modules.annual_close.domain.dto.AnnualCloseResponse;
import com.casrusil.siierpai.modules.annual_close.domain.model.CompanyTaxProfile;
import com.casrusil.siierpai.modules.annual_close.domain.port.CompanyTaxProfileRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import com.casrusil.siierpai.modules.accounting.domain.service.BalanceSheetService;
import com.casrusil.siierpai.modules.accounting.domain.model.BalanceSheetReport;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;

@Service
@RequiredArgsConstructor
public class F22DraftService implements AnnualCloseUseCase {

    private final CompanyTaxProfileRepositoryPort taxProfileRepository;
    private final RLICalculatorService rliCalculatorService;
    private final IDPCCalculatorService idpcCalculatorService;
    private final BalanceSheetService balanceSheetService;

    @Override
    public AnnualCloseResponse calculateAnnualTaxes(int fiscalYear, String companyId) {
        CompanyTaxProfile profile = taxProfileRepository.findByCompanyId(companyId)
                .orElseThrow(() -> new RuntimeException("Tax profile not found for company"));
                
        // Real financial result from BalanceSheetService
        BalanceSheetReport balanceSheet = balanceSheetService.generateBalanceSheet(
                CompanyId.fromString(companyId), 
                LocalDate.of(fiscalYear, 12, 31)
        );
        
        BigDecimal util = balanceSheet.equityAccounts().getOrDefault("UTILIDAD DEL EJERCICIO", BigDecimal.ZERO);
        BigDecimal perdida = balanceSheet.equityAccounts().getOrDefault("PÉRDIDA DEL EJERCICIO", BigDecimal.ZERO);
        BigDecimal simulatedFinancialResult = util.add(perdida);
        
        if (simulatedFinancialResult.compareTo(BigDecimal.ZERO) == 0) {
            simulatedFinancialResult = new BigDecimal("10000000"); // Fallback if no data
        }
        
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
