package com.casrusil.siierpai.modules.accounting.domain.service;

import com.casrusil.siierpai.modules.accounting.domain.model.AccountingEntry;
import com.casrusil.siierpai.modules.accounting.domain.model.AccountingEntryLine;
import com.casrusil.siierpai.modules.accounting.domain.model.ClosedPeriod;
import com.casrusil.siierpai.modules.accounting.domain.port.out.AccountingEntryRepository;
import com.casrusil.siierpai.modules.accounting.domain.port.out.ClosedPeriodRepository;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import com.casrusil.siierpai.shared.domain.valueobject.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for closing accounting periods.
 * Implements Chilean accounting standards for monthly closing.
 */
@Service
public class PeriodClosingService {

    // Chilean Account Codes
    private static final String UTILIDAD_EJERCICIO = "3302"; // Current period earnings
    private static final String REVENUE_PREFIX = "4"; // All revenue accounts start with 4
    private static final String EXPENSE_PREFIX = "5"; // All expense accounts start with 5

    private final ClosedPeriodRepository closedPeriodRepository;
    private final AccountingEntryRepository accountingEntryRepository;
    private final AccountingEntryService accountingEntryService;

    public PeriodClosingService(ClosedPeriodRepository closedPeriodRepository,
            AccountingEntryRepository accountingEntryRepository,
            AccountingEntryService accountingEntryService) {
        this.closedPeriodRepository = closedPeriodRepository;
        this.accountingEntryRepository = accountingEntryRepository;
        this.accountingEntryService = accountingEntryService;
    }

    /**
     * Close an accounting period.
     * This will:
     * 1. Calculate profit/loss for the period
     * 2. Create closing entry transferring P&L to equity
     * 3. Lock the period to prevent future modifications
     * 
     * @param companyId Company identifier
     * @param period    Year-Month to close
     * @param userId    User performing the closing
     * @return ClosedPeriod with profit/loss information
     */
    @Transactional
    public ClosedPeriod closePeriod(CompanyId companyId, YearMonth period, UserId userId) {
        // Validate period is not already closed
        if (isPeriodClosed(companyId, period)) {
            throw new IllegalStateException("Period " + period + " is already closed for company " + companyId.value());
        }

        // Calcular totales del periodo (desglosados por cuenta) y resultado.
        PeriodTotals totals = computePeriodTotals(companyId, period);
        BigDecimal profitLoss = totals.profitLoss();

        // Crear asiento de cierre saldando cada cuenta de resultado individual.
        createClosingEntry(companyId, period, totals);

        // Save closed period
        ClosedPeriod closedPeriod = new ClosedPeriod(companyId, period, userId, profitLoss);
        return closedPeriodRepository.save(closedPeriod);
    }

    /**
     * Check if a period is closed.
     */
    public boolean isPeriodClosed(CompanyId companyId, YearMonth period) {
        return closedPeriodRepository.exists(companyId, period);
    }

    /**
     * Validate that a period is open (not closed).
     * Throws exception if period is closed.
     */
    public void validatePeriodOpen(CompanyId companyId, YearMonth period) {
        if (isPeriodClosed(companyId, period)) {
            throw new IllegalStateException("Accounting period " + period + " is closed and cannot be modified.");
        }
    }

    /**
     * Get all closed periods for a company.
     */
    public List<ClosedPeriod> getClosedPeriods(CompanyId companyId) {
        return closedPeriodRepository.findByCompanyId(companyId);
    }

    /**
     * Totales del periodo, desglosados por cuenta, para construir el asiento de cierre.
     *
     * @param revenueByAccount saldo acreedor por cada cuenta de resultado-ganancia (prefijo 4)
     * @param expenseByAccount saldo deudor por cada cuenta de resultado-pérdida (prefijo 5)
     */
    private record PeriodTotals(
            java.util.Map<String, BigDecimal> revenueByAccount,
            java.util.Map<String, BigDecimal> expenseByAccount) {

        BigDecimal totalRevenue() {
            return revenueByAccount.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        BigDecimal totalExpenses() {
            return expenseByAccount.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        BigDecimal profitLoss() {
            return totalRevenue().subtract(totalExpenses());
        }
    }

    /**
     * Recorre los asientos del periodo y acumula, por cuenta, el saldo de las
     * cuentas de resultado (ingresos = créditos de cuentas "4", gastos = débitos
     * de cuentas "5"). Se usa tanto para el resultado del ejercicio como para
     * construir un asiento de cierre que salda cada cuenta individual.
     */
    private PeriodTotals computePeriodTotals(CompanyId companyId, YearMonth period) {
        List<AccountingEntry> entries = accountingEntryRepository.findByCompanyId(companyId);

        LocalDate start = period.atDay(1);
        LocalDate end = period.atEndOfMonth();

        java.util.Map<String, BigDecimal> revenueByAccount = new java.util.TreeMap<>();
        java.util.Map<String, BigDecimal> expenseByAccount = new java.util.TreeMap<>();

        for (AccountingEntry entry : entries) {
            LocalDate entryDate = entry.getEntryDate();
            if (entryDate.isBefore(start) || entryDate.isAfter(end)) {
                continue;
            }
            for (AccountingEntryLine line : entry.getLines()) {
                String accountCode = line.accountCode();
                if (accountCode.startsWith(REVENUE_PREFIX)) {
                    revenueByAccount.merge(accountCode, line.credit(), BigDecimal::add);
                } else if (accountCode.startsWith(EXPENSE_PREFIX)) {
                    expenseByAccount.merge(accountCode, line.debit(), BigDecimal::add);
                }
            }
        }
        return new PeriodTotals(revenueByAccount, expenseByAccount);
    }

    /**
     * Crea el asiento de cierre que salda TODAS las cuentas de resultado individuales
     * y transfiere el neto al patrimonio (Utilidad del Ejercicio):
     * <ul>
     *   <li>Debita cada cuenta de ingresos por su saldo acreedor (la deja en cero).</li>
     *   <li>Acredita cada cuenta de gastos por su saldo deudor (la deja en cero).</li>
     *   <li>La diferencia (resultado) se lleva a {@code UTILIDAD_EJERCICIO}
     *       (crédito si hay utilidad, débito si hay pérdida).</li>
     * </ul>
     * El asiento queda balanceado (débitos = créditos) por construcción.
     */
    private void createClosingEntry(CompanyId companyId, YearMonth period, PeriodTotals totals) {
        List<AccountingEntryLine> lines = new ArrayList<>();

        // Saldar cada cuenta de ingresos (débito por su saldo acreedor).
        totals.revenueByAccount().forEach((account, balance) -> {
            if (balance.signum() != 0) {
                lines.add(AccountingEntryLine.debit(account, "Cierre de ingresos " + period, balance));
            }
        });

        // Saldar cada cuenta de gastos (crédito por su saldo deudor).
        totals.expenseByAccount().forEach((account, balance) -> {
            if (balance.signum() != 0) {
                lines.add(AccountingEntryLine.credit(account, "Cierre de gastos " + period, balance));
            }
        });

        if (lines.isEmpty()) {
            return; // Sin movimientos de resultado: no hay nada que cerrar.
        }

        // Línea de balance hacia el patrimonio (Utilidad del Ejercicio).
        BigDecimal profitLoss = totals.profitLoss();
        if (profitLoss.signum() > 0) {
            lines.add(AccountingEntryLine.credit(UTILIDAD_EJERCICIO, "Utilidad del Ejercicio " + period, profitLoss));
        } else if (profitLoss.signum() < 0) {
            lines.add(AccountingEntryLine.debit(UTILIDAD_EJERCICIO, "Pérdida del Ejercicio " + period, profitLoss.abs()));
        }

        AccountingEntry closingEntry = new AccountingEntry(
                companyId,
                "Closing Entry - " + period,
                period.toString(),
                "PERIOD_CLOSING",
                null,
                null,
                null,
                null,
                "POSTED",
                lines,
                com.casrusil.siierpai.modules.accounting.domain.model.EntryType.CLOSING);

        accountingEntryService.recordEntry(closingEntry);
    }
}
