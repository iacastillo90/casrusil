package com.casrusil.siierpai.modules.sustainability.application.listener;

import com.casrusil.siierpai.modules.accounting.domain.model.AccountingEntry;
import com.casrusil.siierpai.modules.accounting.domain.model.AccountingEntryLine;
import com.casrusil.siierpai.modules.accounting.domain.port.out.AccountingEntryRepository;
import com.casrusil.siierpai.modules.invoicing.domain.event.InvoiceCreatedEvent;
import com.casrusil.siierpai.modules.invoicing.domain.model.Invoice;
import com.casrusil.siierpai.modules.sustainability.domain.model.GreenMetrics;
import com.casrusil.siierpai.modules.sustainability.domain.port.out.GreenMetricsRepository;
import com.casrusil.siierpai.modules.sustainability.domain.service.SustainabilityClassifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 🌿 FERRARI ENGINE: Listener que calcula el impacto ambiental en tiempo real.
 * 
 * <p>
 * Este listener se ejecuta en paralelo con {@code InvoiceAccountingListener}
 * y {@code PartnerLedgerListener}, utilizando Virtual Threads para no bloquear
 * el flujo principal de importación.
 * 
 * <h2>Flujo de Ejecución:</h2>
 * <ol>
 * <li>Se crea una factura (manual o CSV import)</li>
 * <li>Se publica {@code InvoiceCreatedEvent}</li>
 * <li>Este listener se ejecuta ASÍNCRONAMENTE en un Virtual Thread</li>
 * <li>Se clasifica el impacto ambiental de la transacción</li>
 * <li>Se persisten las métricas verdes en la BD</li>
 * <li>El usuario puede consultar su "Pasaporte Verde" actualizado</li>
 * </ol>
 * 
 * @see InvoiceCreatedEvent
 * @see SustainabilityClassifier
 * @see GreenMetrics
 * @since 2.0 - Ferrari Engine Phase 2
 */
@Component
public class SustainabilityListener {

    private static final Logger logger = LoggerFactory.getLogger(SustainabilityListener.class);

    private final SustainabilityClassifier classifier;
    private final GreenMetricsRepository greenMetricsRepository;
    private final AccountingEntryRepository accountingEntryRepository;

    public SustainabilityListener(
            SustainabilityClassifier classifier,
            GreenMetricsRepository greenMetricsRepository,
            AccountingEntryRepository accountingEntryRepository) {
        this.classifier = classifier;
        this.greenMetricsRepository = greenMetricsRepository;
        this.accountingEntryRepository = accountingEntryRepository;
    }

    /**
     * Maneja el evento de factura creada y calcula métricas ambientales.
     * 
     * <p>
     * Este método se ejecuta asíncronamente en un Virtual Thread, permitiendo
     * procesamiento paralelo sin afectar el rendimiento de la importación.
     * 
     * @param event Evento que contiene la factura recién creada
     */
    @EventListener
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handle(InvoiceCreatedEvent event) {
        Invoice invoice = event.invoice();

        try {
            logger.info("🌿 [SUSTAINABILITY] Analizando impacto ambiental: Factura {} - {}",
                    invoice.getFolio(), invoice.getBusinessName());

            // Esperar un momento para que el asiento contable se haya creado
            // (InvoiceAccountingListener se ejecuta en paralelo)
            Thread.sleep(200);

            // Buscar el asiento contable generado
            Optional<AccountingEntry> entryOpt = findAccountingEntry(invoice);

            if (entryOpt.isEmpty()) {
                logger.warn(
                        "⚠️ [SUSTAINABILITY] No se encontró asiento contable para factura {}. Clasificando sin entry ID.",
                        invoice.getFolio());
            }

            // Clasificar cada línea del asiento (o la factura completa si no hay asiento)
            if (entryOpt.isPresent()) {
                AccountingEntry entry = entryOpt.get();
                for (AccountingEntryLine line : entry.getLines()) {
                    classifyAndSave(invoice, line.accountCode(), entry.getId());
                }
            } else {
                // Clasificar solo basándose en la factura
                classifyAndSave(invoice, null, null);
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("❌ [SUSTAINABILITY] Thread interrumpido: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("❌ [SUSTAINABILITY] Error calculando métricas verdes para factura {}: {}",
                    invoice.getFolio(), e.getMessage(), e);
            throw e; // Re-throw para que AsyncUncaughtExceptionHandler lo capture
        }
    }

    /**
     * Clasifica y persiste las métricas ambientales.
     */
    private void classifyAndSave(Invoice invoice, String accountCode, java.util.UUID accountingEntryId) {
        Optional<GreenMetrics> metricsOpt = classifier.classify(invoice, accountCode);

        if (metricsOpt.isPresent()) {
            GreenMetrics metrics = metricsOpt.get();

            // Si tenemos el ID del asiento, crear una nueva instancia con ese ID
            if (accountingEntryId != null) {
                metrics = GreenMetrics.create(
                        metrics.getCompanyId(),
                        accountingEntryId,
                        metrics.getCategory(),
                        metrics.getCarbonFootprint(),
                        metrics.getGreenScore(),
                        metrics.getEvidence());
            }

            greenMetricsRepository.save(metrics);

            logger.info("✅ [SUSTAINABILITY] Métrica guardada: {} | Huella: {} kg CO2e | Score: {}",
                    metrics.getCategory(),
                    metrics.getCarbonFootprint(),
                    metrics.getGreenScore());
        }
    }

    /**
     * Busca el asiento contable generado para esta factura.
     */
    private Optional<AccountingEntry> findAccountingEntry(Invoice invoice) {
        return accountingEntryRepository.findByCompanyId(invoice.getCompanyId())
                .stream()
                .filter(entry -> invoice.getFolio().toString().equals(entry.getDocumentNumber()))
                .findFirst();
    }
}
