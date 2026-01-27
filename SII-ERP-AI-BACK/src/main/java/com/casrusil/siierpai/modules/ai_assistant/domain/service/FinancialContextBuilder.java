package com.casrusil.siierpai.modules.ai_assistant.domain.service;

import com.casrusil.siierpai.modules.accounting.application.service.CashFlowProjectionService;
import com.casrusil.siierpai.modules.accounting.application.service.DuplicateInvoiceDetector;
import com.casrusil.siierpai.modules.accounting.application.service.SuspiciousAmountDetector;
import com.casrusil.siierpai.modules.accounting.domain.model.AuditAlert;
import com.casrusil.siierpai.modules.accounting.domain.model.DraftF29;
import com.casrusil.siierpai.modules.accounting.domain.service.F29CalculatorService;
import com.casrusil.siierpai.modules.invoicing.domain.model.Invoice;
import com.casrusil.siierpai.modules.invoicing.domain.port.in.SearchInvoicesUseCase;
import com.casrusil.siierpai.modules.sustainability.infrastructure.persistence.repository.SustainabilityRecordRepository;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Servicio que construye el contexto financiero real para el Financial Advisor.
 * Extrae datos de múltiples fuentes (F29, Facturas, Auditoría, Cash Flow) y
 * los formatea para consumo de la IA.
 */
@Service
public class FinancialContextBuilder {

        private final F29CalculatorService f29CalculatorService;
        private final SearchInvoicesUseCase searchInvoicesUseCase;
        private final CashFlowProjectionService cashFlowProjectionService;
        private final DuplicateInvoiceDetector duplicateDetector;
        private final SuspiciousAmountDetector suspiciousAmountDetector;
        private final SustainabilityRecordRepository sustainabilityRepository;

        public FinancialContextBuilder(F29CalculatorService f29CalculatorService,
                        SearchInvoicesUseCase searchInvoicesUseCase,
                        CashFlowProjectionService cashFlowProjectionService,
                        DuplicateInvoiceDetector duplicateDetector,
                        SuspiciousAmountDetector suspiciousAmountDetector,
                        SustainabilityRecordRepository sustainabilityRepository) {
                this.f29CalculatorService = f29CalculatorService;
                this.searchInvoicesUseCase = searchInvoicesUseCase;
                this.cashFlowProjectionService = cashFlowProjectionService;
                this.duplicateDetector = duplicateDetector;
                this.suspiciousAmountDetector = suspiciousAmountDetector;
                this.sustainabilityRepository = sustainabilityRepository;
        }

        /**
         * Construye el contexto financiero completo con datos reales.
         */
        public String buildDailyContext(CompanyId companyId) {
                YearMonth currentMonth = YearMonth.now();
                LocalDate today = LocalDate.now();

                // 1. Situación Fiscal (F29) - DATOS REALES
                DraftF29 f29Draft = f29CalculatorService.calculateDraftF29(companyId, currentMonth);
                var f29 = f29Draft.report();

                // 2. Análisis de Facturas
                List<Invoice> allInvoices = searchInvoicesUseCase.getInvoicesByCompany(companyId);

                List<Invoice> currentMonthInvoices = allInvoices.stream()
                                .filter(inv -> YearMonth.from(inv.getDate()).equals(currentMonth))
                                .toList();

                // Facturas potencialmente vencidas (>30 días)
                List<Invoice> overdueInvoices = allInvoices.stream()
                                .filter(inv -> inv.getDate().isBefore(today.minusDays(30)))
                                .toList();

                BigDecimal totalOverdue = overdueInvoices.stream()
                                .map(Invoice::getTotalAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                // 3. Proyección de Flujo de Caja (3 meses)
                CashFlowProjectionService.CashFlowProjection cashFlowProjection = cashFlowProjectionService
                                .projectCashFlow(companyId, 3);

                // 4. Alertas de Auditoría
                List<AuditAlert> duplicates = duplicateDetector.detectDuplicates(companyId);
                List<AuditAlert> suspicious = suspiciousAmountDetector.detectSuspiciousAmounts(companyId);

                long criticalAlerts = duplicates.stream()
                                .filter(a -> a.getSeverity() == AuditAlert.Severity.CRITICAL)
                                .count()
                                + suspicious.stream()
                                                .filter(a -> a.getSeverity() == AuditAlert.Severity.CRITICAL)
                                                .count();

                // 5. Construir el contexto formateado
                StringBuilder context = new StringBuilder();

                context.append(String.format("""
                                📅 DATOS FINANCIEROS REALES AL %s:

                                💰 SITUACIÓN FISCAL (F29 EN TIEMPO REAL):
                                   - Período: %s
                                   - Ventas Netas: $%s
                                   - Compras Netas: $%s
                                   - IVA Débito (Ventas): $%s
                                   - IVA Crédito (Compras): $%s
                                   - 💸 IMPUESTO A PAGAR ESTIMADO: $%s
                                """,
                                today.format(DateTimeFormatter.ISO_DATE),
                                currentMonth,
                                formatMoney(f29.totalSalesTaxable()),
                                formatMoney(f29.totalPurchasesTaxable()),
                                formatMoney(f29.vatDebit()),
                                formatMoney(f29.vatCredit()),
                                formatMoney(f29.vatPayable())));

                context.append(String.format("""

                                📊 FLUJO DE CAJA Y FACTURACIÓN:
                                   - Facturas emitidas este mes: %d
                                   - Facturas vencidas (>30 días): %d
                                   - Monto total en riesgo de no pago: $%s
                                """,
                                currentMonthInvoices.size(),
                                overdueInvoices.size(),
                                formatMoney(totalOverdue)));

                // Proyección de caja
                if (cashFlowProjection.hasNegativeMonths()) {
                        YearMonth firstNegative = cashFlowProjection.firstNegativeMonth();
                        context.append(String.format("""

                                        ⚠️ ALERTA DE LIQUIDEZ:
                                           - Proyección: Posible déficit en %s
                                           - Promedio mensual: Ingresos $%s | Egresos $%s
                                        """,
                                        firstNegative,
                                        formatMoney(cashFlowProjection.avgMonthlyInflow()),
                                        formatMoney(cashFlowProjection.avgMonthlyOutflow())));
                } else {
                        context.append(String.format("""

                                        ✅ LIQUIDEZ SALUDABLE:
                                           - Proyección 3 meses: Sin déficit esperado
                                           - Promedio mensual: Ingresos $%s | Egresos $%s
                                        """,
                                        formatMoney(cashFlowProjection.avgMonthlyInflow()),
                                        formatMoney(cashFlowProjection.avgMonthlyOutflow())));
                }

                // Alertas de auditoría
                if (criticalAlerts > 0 || !duplicates.isEmpty() || !suspicious.isEmpty()) {
                        context.append(String.format("""

                                        🚨 ALERTAS DE AUDITORÍA:
                                           - Alertas críticas: %d
                                           - Facturas duplicadas detectadas: %d
                                           - Montos sospechosos detectados: %d
                                        """,
                                        criticalAlerts,
                                        duplicates.size(),
                                        suspicious.size()));
                }

                // Warnings del F29
                if (!f29Draft.warnings().isEmpty()) {
                        context.append("\n⚠️ ADVERTENCIAS FISCALES:\n");
                        for (var warning : f29Draft.warnings()) {
                                context.append("   - ").append(warning.message()).append("\n");
                        }
                }

                // 6. Métricas de Sostenibilidad (Nuevo)
                LocalDateTime startMonth = currentMonth.atDay(1).atStartOfDay();
                LocalDateTime endMonth = currentMonth.atEndOfMonth().atTime(23, 59, 59);
                BigDecimal monthlyCarbon = sustainabilityRepository.sumCarbonFootprintBetween(startMonth, endMonth);
                if (monthlyCarbon == null)
                        monthlyCarbon = BigDecimal.ZERO;

                context.append(String.format("""

                                🌱 IMPACTO AMBIENTAL ESTIMADO (MES ACTUAL):
                                   - Huella de Carbono: %s kgCO2e
                                   - Estado: %s
                                """,
                                formatMoney(monthlyCarbon),
                                monthlyCarbon.doubleValue() < 1000 ? "🟢 Bajo Impacto" : "🟡 Impacto Moderado"));

                return context.toString();
        }

        private String formatMoney(BigDecimal amount) {
                if (amount == null) {
                        return "0";
                }
                return String.format("%,.0f", amount);
        }
}
