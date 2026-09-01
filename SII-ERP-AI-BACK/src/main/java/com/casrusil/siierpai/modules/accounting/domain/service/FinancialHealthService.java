package com.casrusil.siierpai.modules.accounting.domain.service;

import com.casrusil.siierpai.modules.accounting.domain.model.BalanceSheetReport;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

/**
 * Calcula un índice de "salud financiera" (0-100) a partir del Balance General real.
 *
 * <p>Reemplaza el valor hardcodeado ({@code 75.0}) que se usaba como placeholder en
 * varios controladores. El índice se deriva del ratio Activos/Pasivos:
 * <ul>
 *   <li>Con pasivos &gt; 0: {@code min(100, (activos/pasivos) * 50)} — un ratio de 2:1
 *       (saludable) produce 100.</li>
 *   <li>Sin pasivos pero con activos: 100 (sin apalancamiento).</li>
 *   <li>Sin activos ni pasivos (empresa sin datos): 50 (neutro).</li>
 * </ul>
 *
 * <p>Centraliza la fórmula para que Compliance y Sustainability usen exactamente
 * el mismo cálculo (antes duplicado).
 */
@Service
public class FinancialHealthService {

    private static final BigDecimal RATIO_FACTOR = new BigDecimal("50");
    private static final BigDecimal MAX_HEALTH = new BigDecimal("100");
    private static final BigDecimal NEUTRAL_HEALTH = new BigDecimal("50");

    private final BalanceSheetService balanceSheetService;

    public FinancialHealthService(BalanceSheetService balanceSheetService) {
        this.balanceSheetService = balanceSheetService;
    }

    /**
     * @param companyId empresa
     * @param asOfDate  fecha de corte del balance
     * @return índice de salud financiera entre 0 y 100
     */
    public BigDecimal calculate(CompanyId companyId, LocalDate asOfDate) {
        BalanceSheetReport balanceSheet = balanceSheetService.generateBalanceSheet(companyId, asOfDate);
        return calculateFromBalance(balanceSheet);
    }

    /** Variante que reutiliza un balance ya generado (evita recalcularlo). */
    public BigDecimal calculateFromBalance(BalanceSheetReport balanceSheet) {
        BigDecimal totalAssets = balanceSheet.totalAssets();
        BigDecimal totalLiabilities = balanceSheet.totalLiabilities();

        if (totalLiabilities.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal ratio = totalAssets.divide(totalLiabilities, 2, RoundingMode.HALF_UP);
            return ratio.multiply(RATIO_FACTOR).min(MAX_HEALTH);
        }
        if (totalAssets.compareTo(BigDecimal.ZERO) > 0) {
            return MAX_HEALTH;
        }
        return NEUTRAL_HEALTH;
    }
}
