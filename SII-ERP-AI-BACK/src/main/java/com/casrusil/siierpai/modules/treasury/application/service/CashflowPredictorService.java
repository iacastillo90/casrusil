package com.casrusil.siierpai.modules.treasury.application.service;

import com.casrusil.siierpai.modules.accounting.domain.model.AccountMovement;
import com.casrusil.siierpai.modules.accounting.domain.service.AccountingEntryService;
import com.casrusil.siierpai.modules.invoicing.domain.model.Invoice;
import com.casrusil.siierpai.modules.invoicing.domain.model.TransactionType;
import com.casrusil.siierpai.modules.invoicing.domain.port.out.InvoiceRepository;
import com.casrusil.siierpai.modules.treasury.domain.model.CashFlowForecast;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio de proyección de flujo de caja basado en datos reales del sistema.
 *
 * <p>A diferencia de la versión anterior (que usaba valores placeholder en 0),
 * esta implementación lee:
 * <ul>
 *   <li><b>Saldo actual</b>: saldo acumulado de la cuenta Caja (110101) desde el Libro Mayor.</li>
 *   <li><b>Cuentas por cobrar</b>: facturas de venta (SALE) pendientes de pago.</li>
 *   <li><b>Cuentas por pagar</b>: facturas de compra (PURCHASE) pendientes de pago.</li>
 *   <li><b>Costos fijos mensuales</b>: estimados a partir del egreso de caja promedio
 *       de los últimos 3 meses (dato real, no configuración hardcodeada).</li>
 * </ul>
 *
 * <p>Se aplica una probabilidad de cobranza sobre las cuentas por cobrar para
 * proyectar el saldo a 30 días.
 */
@Service
public class CashflowPredictorService {

    /** Cuenta contable de Caja (plan de cuentas chileno del sistema). */
    private static final String CASH_ACCOUNT = "110101";

    /** Probabilidad histórica de cobranza a tiempo aplicada a las cuentas por cobrar. */
    private static final BigDecimal COLLECTION_PROBABILITY = new BigDecimal("0.80");

    /** Umbral de saldo proyectado bajo el cual se advierte riesgo de liquidez. */
    private static final BigDecimal LIQUIDITY_RISK_THRESHOLD = new BigDecimal("10000000");

    private final InvoiceRepository invoiceRepository;
    private final AccountingEntryService accountingEntryService;

    public CashflowPredictorService(InvoiceRepository invoiceRepository,
            AccountingEntryService accountingEntryService) {
        this.invoiceRepository = invoiceRepository;
        this.accountingEntryService = accountingEntryService;
    }

    public CashFlowForecast generateProbabilisticForecast(CompanyId companyId) {
        LocalDate today = LocalDate.now();

        BigDecimal currentBalance = currentCashBalance(companyId, today);
        List<Invoice> openInvoices = invoiceRepository.findOpenInvoices(companyId);
        BigDecimal pendingReceivables = sumByTransactionType(openInvoices, TransactionType.SALE);
        BigDecimal pendingPayables = sumByTransactionType(openInvoices, TransactionType.PURCHASE);
        BigDecimal fixedMonthlyCosts = estimateFixedMonthlyCosts(companyId, today);

        // Cobranza esperada: aplicamos la probabilidad histórica de pago a tiempo.
        BigDecimal expectedInflow = pendingReceivables.multiply(COLLECTION_PROBABILITY);
        // Egresos esperados: se asume el pago del 100% de las deudas + costos fijos.
        BigDecimal expectedOutflow = pendingPayables.add(fixedMonthlyCosts);

        BigDecimal projectedBalance = currentBalance.add(expectedInflow).subtract(expectedOutflow);

        List<CashFlowForecast.RiskFactor> risks = new ArrayList<>();
        if (projectedBalance.compareTo(LIQUIDITY_RISK_THRESHOLD) < 0) {
            risks.add(new CashFlowForecast.RiskFactor(
                    "ALTO RIESGO DE LIQUIDEZ",
                    "Si la morosidad de clientes supera el 20%, no se podrán cubrir los costos fijos."));
        }

        return new CashFlowForecast(
                today.plusDays(30),
                currentBalance,
                expectedInflow,
                expectedOutflow,
                projectedBalance,
                risks);
    }

    /** Saldo acumulado de la cuenta Caja a la fecha (último saldo del Libro Mayor del año en curso). */
    private BigDecimal currentCashBalance(CompanyId companyId, LocalDate today) {
        List<AccountMovement> ledger = accountingEntryService.getLedger(
                companyId, CASH_ACCOUNT, today.withDayOfYear(1), today);
        if (ledger.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return ledger.get(ledger.size() - 1).balance();
    }

    /**
     * Estima el costo fijo mensual como el promedio del egreso de caja (créditos de
     * la cuenta Caja) de los últimos 3 meses. Es un dato derivado del histórico real.
     */
    private BigDecimal estimateFixedMonthlyCosts(CompanyId companyId, LocalDate today) {
        LocalDate from = today.minusMonths(3);
        List<AccountMovement> ledger = accountingEntryService.getLedger(companyId, CASH_ACCOUNT, from, today);
        BigDecimal totalOutflow = ledger.stream()
                .map(AccountMovement::credit)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return totalOutflow.divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal sumByTransactionType(List<Invoice> invoices, TransactionType type) {
        return invoices.stream()
                .filter(i -> i.getTransactionType() == type)
                .map(Invoice::getTotalAmount)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
