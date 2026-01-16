package com.casrusil.siierpai.modules.accounting.domain.service;

import com.casrusil.siierpai.modules.accounting.domain.model.AnomalyWarning;
import com.casrusil.siierpai.modules.accounting.domain.model.F29Report;
import com.casrusil.siierpai.modules.invoicing.domain.model.Invoice;
import com.casrusil.siierpai.modules.invoicing.domain.port.out.InvoiceRepository;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 🧠 FERRARI ENGINE PHASE 4: Servicio de detección de anomalías mejorado.
 * 
 * <p>
 * Analiza reportes financieros y facturas para identificar situaciones
 * inusuales o potencialmente erróneas antes de la declaración.
 * 
 * <h2>Reglas Aplicadas:</h2>
 * <ul>
 * <li><strong>Margen Negativo:</strong> IVA Crédito > Débito</li>
 * <li><strong>Volumen de Compras:</strong> Compras > $10M</li>
 * <li><strong>Pico de IVA:</strong> Débito > 150% del promedio</li>
 * <li><strong>Facturas Duplicadas:</strong> Mismo RUT + Folio + Monto</li>
 * <li><strong>Gastos Inusuales:</strong> Gastos > 200% del promedio</li>
 * </ul>
 * 
 * @see AnomalyWarning
 * @see F29Report
 * @since 2.0 - Ferrari Engine Phase 4
 */
@Service
public class AnomalyDetectionService {

    private static final BigDecimal HIGH_PURCHASE_THRESHOLD = new BigDecimal("10000000"); // 10 Million
    private static final BigDecimal VAT_SPIKE_THRESHOLD_PERCENT = new BigDecimal("1.50"); // 50% increase
    private static final BigDecimal EXPENSE_SPIKE_THRESHOLD = new BigDecimal("2.0"); // 200% of average

    private final InvoiceRepository invoiceRepository;

    public AnomalyDetectionService(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    /**
     * Detecta anomalías en el reporte F29 actual.
     */
    public List<AnomalyWarning> detectAnomalies(F29Report currentReport) {
        List<AnomalyWarning> warnings = new ArrayList<>();

        // Rule 1: Negative Margin (VAT Credit > VAT Debit)
        if (currentReport.vatCredit().compareTo(currentReport.vatDebit()) > 0) {
            warnings.add(new AnomalyWarning(
                    "NEG_MARGIN",
                    "El IVA Crédito supera al IVA Débito. ¿Tuviste más compras que ventas este mes?",
                    AnomalyWarning.Severity.WARNING));
        }

        // Rule 2: High Purchase Volume
        if (currentReport.totalPurchasesTaxable().compareTo(HIGH_PURCHASE_THRESHOLD) > 0) {
            warnings.add(new AnomalyWarning(
                    "HIGH_PURCHASE",
                    "El monto de compras netas supera los $10.000.000. Verifica que todas las facturas sean del giro.",
                    AnomalyWarning.Severity.INFO));
        }

        // Rule 3: VAT Debit Spike (Mocked history comparison)
        BigDecimal mockedAverageVatDebit = new BigDecimal("500000");
        if (currentReport.vatDebit().compareTo(mockedAverageVatDebit.multiply(VAT_SPIKE_THRESHOLD_PERCENT)) > 0) {
            warnings.add(new AnomalyWarning(
                    "VAT_SPIKE",
                    "El IVA Débito es un 50% mayor al promedio histórico estimado ($500.000).",
                    AnomalyWarning.Severity.WARNING));
        }

        return warnings;
    }

    /**
     * 🆕 FERRARI ENGINE: Detecta facturas duplicadas en un período.
     * 
     * <p>
     * Criterio de duplicado: Mismo RUT emisor + Folio + Monto total
     * 
     * @param companyId ID de la empresa
     * @param period    Período a analizar
     * @return Lista de facturas duplicadas
     */
    public List<Invoice> detectDuplicateInvoices(CompanyId companyId, YearMonth period) {
        List<Invoice> allInvoices = invoiceRepository.findByCompanyId(companyId);

        // Filtrar por período
        List<Invoice> periodInvoices = allInvoices.stream()
                .filter(inv -> YearMonth.from(inv.getDate()).equals(period))
                .toList();

        // Agrupar por clave única: RUT + Folio + Monto
        Map<String, List<Invoice>> grouped = periodInvoices.stream()
                .collect(Collectors
                        .groupingBy(inv -> inv.getIssuerRut() + "|" + inv.getFolio() + "|" + inv.getTotalAmount()));

        // Retornar solo los grupos con más de 1 factura
        return grouped.values().stream()
                .filter(list -> list.size() > 1)
                .flatMap(List::stream)
                .toList();
    }

    /**
     * 🆕 FERRARI ENGINE: Detecta gastos inusuales comparados con el promedio.
     * 
     * @param companyId     ID de la empresa
     * @param currentPeriod Período actual
     * @return Lista de advertencias de gastos inusuales
     */
    public List<AnomalyWarning> detectUnusualExpenses(CompanyId companyId, YearMonth currentPeriod) {
        List<AnomalyWarning> warnings = new ArrayList<>();

        // TODO: Implementar cálculo de promedio histórico
        // Por ahora, usar valor mockeado
        BigDecimal averageExpenses = new BigDecimal("5000000"); // $5M promedio

        List<Invoice> currentInvoices = invoiceRepository.findByCompanyId(companyId).stream()
                .filter(inv -> YearMonth.from(inv.getDate()).equals(currentPeriod))
                .filter(inv -> inv.getTransactionType().toString().equals("PURCHASE"))
                .toList();

        BigDecimal currentExpenses = currentInvoices.stream()
                .map(Invoice::getNetAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (currentExpenses.compareTo(averageExpenses.multiply(EXPENSE_SPIKE_THRESHOLD)) > 0) {
            warnings.add(new AnomalyWarning(
                    "EXPENSE_SPIKE",
                    String.format("Gastos del mes ($%s) son 200%% mayores al promedio ($%s). Revisar compras.",
                            currentExpenses, averageExpenses),
                    AnomalyWarning.Severity.WARNING));
        }

        return warnings;
    }

    /**
     * 🆕 FERRARI ENGINE: Análisis completo de anomalías para el dashboard.
     * 
     * @param companyId ID de la empresa
     * @param period    Período a analizar
     * @param f29Report Reporte F29 del período
     * @return Lista consolidada de todas las anomalías detectadas
     */
    public List<AnomalyWarning> detectAllAnomalies(CompanyId companyId, YearMonth period, F29Report f29Report) {
        List<AnomalyWarning> allWarnings = new ArrayList<>();

        // 1. Anomalías del F29
        allWarnings.addAll(detectAnomalies(f29Report));

        // 2. Facturas duplicadas
        List<Invoice> duplicates = detectDuplicateInvoices(companyId, period);
        if (!duplicates.isEmpty()) {
            allWarnings.add(new AnomalyWarning(
                    "DUPLICATE_INVOICES",
                    String.format("Se detectaron %d facturas duplicadas. Revisar importaciones.", duplicates.size()),
                    AnomalyWarning.Severity.CRITICAL));
        }

        // 3. Gastos inusuales
        allWarnings.addAll(detectUnusualExpenses(companyId, period));

        return allWarnings;
    }
}
