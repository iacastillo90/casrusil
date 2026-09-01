package com.casrusil.siierpai.modules.annual_close.application.service;

import com.casrusil.siierpai.modules.annual_close.domain.dto.AnnualCloseResponse;
import com.casrusil.siierpai.modules.annual_close.domain.model.CompanyTaxProfile;
import com.casrusil.siierpai.modules.annual_close.domain.port.CompanyTaxProfileRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import com.casrusil.siierpai.modules.accounting.domain.service.BalanceSheetService;
import com.casrusil.siierpai.modules.accounting.domain.service.F29CalculatorService;
import com.casrusil.siierpai.modules.accounting.domain.model.BalanceSheetReport;
import com.casrusil.siierpai.modules.accounting.domain.model.F29Report;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;

@Service
@RequiredArgsConstructor
public class F22DraftService implements AnnualCloseUseCase {

    private final CompanyTaxProfileRepositoryPort taxProfileRepository;
    private final RLICalculatorService rliCalculatorService;
    private final IDPCCalculatorService idpcCalculatorService;
    private final BalanceSheetService balanceSheetService;
    private final F29CalculatorService f29CalculatorService;

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
        // Resultado financiero real del ejercicio (utilidad - pérdida). Sin fallback ficticio:
        // si no hay datos, el resultado es 0 y así se refleja.
        BigDecimal financialResult = util.add(perdida);

        BigDecimal totalAdditions = rliCalculatorService.calculateTotalAdditions(companyId, fiscalYear);
        BigDecimal totalDeductions = rliCalculatorService.calculateTotalDeductions(companyId, fiscalYear);

        BigDecimal rli = rliCalculatorService.calculateRLI(financialResult, companyId, fiscalYear);

        BigDecimal idpc = idpcCalculatorService.calculateIDPC(rli, profile.getRegime());

        // PPM anual real: suma de los Pagos Provisionales Mensuales de los 12 meses del
        // ejercicio (tasa PPM del contribuyente aplicada a las ventas netas afectas de
        // cada F29). Esto es lo que luego se cruza en el F22 (códigos PPM: 36 / 1645).
        BigDecimal ppmTotal = calculateAnnualPPM(companyId, fiscalYear, profile.getPpmRate());

        BigDecimal taxToPayOrReturn = idpc.subtract(ppmTotal);
        
        return AnnualCloseResponse.builder()
                .companyId(companyId)
                .fiscalYear(fiscalYear)
                .financialResult(financialResult)
                .totalAdditions(totalAdditions)
                .totalDeductions(totalDeductions)
                .rli(rli)
                .calculatedTax(idpc)
                .totalPPM(ppmTotal)
                .taxToPayOrReturn(taxToPayOrReturn)
                .build();
    }

    /**
     * Calcula el total anual de Pagos Provisionales Mensuales (PPM) sumando, para cada
     * uno de los 12 meses del ejercicio, la tasa de PPM del contribuyente aplicada a las
     * ventas netas afectas declaradas en su F29.
     *
     * <p>El monto resultante es el que se pone a disposición en la Operación Renta y se
     * cruza en el F22 (p. ej. código 36 para el propietario, o 1645 en régimen de
     * transparencia). Si el contribuyente no tiene tasa de PPM configurada, el total es 0.
     *
     * @param ppmRate tasa de PPM como fracción (ej. 0.0025 = 0,25%)
     */
    private BigDecimal calculateAnnualPPM(String companyId, int fiscalYear, BigDecimal ppmRate) {
        if (ppmRate == null || ppmRate.signum() <= 0) {
            return BigDecimal.ZERO;
        }
        CompanyId cid = CompanyId.fromString(companyId);
        BigDecimal total = BigDecimal.ZERO;
        for (int month = 1; month <= 12; month++) {
            F29Report f29 = f29CalculatorService.calculateF29(cid, YearMonth.of(fiscalYear, month));
            BigDecimal monthlyTaxableSales = (f29 != null && f29.totalSalesTaxable() != null)
                    ? f29.totalSalesTaxable()
                    : BigDecimal.ZERO;
            total = total.add(monthlyTaxableSales.multiply(ppmRate));
        }
        return total.setScale(0, java.math.RoundingMode.HALF_UP);
    }
}
