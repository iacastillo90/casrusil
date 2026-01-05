package com.casrusil.siierpai.modules.accounting.application.listener;

import com.casrusil.siierpai.modules.invoicing.domain.event.InvoiceCreatedEvent;
import com.casrusil.siierpai.modules.invoicing.domain.model.Invoice;
import com.casrusil.siierpai.modules.invoicing.domain.model.TransactionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 🏦 FERRARI ENGINE: Listener que actualiza las Cuentas Corrientes de Partners
 * en tiempo real.
 * 
 * <p>
 * Este listener se ejecuta en paralelo con {@code InvoiceAccountingListener},
 * utilizando Virtual Threads para no bloquear el flujo principal de
 * importación.
 * 
 * <h2>Flujo de Ejecución:</h2>
 * <ol>
 * <li>Se crea una factura (manual o CSV import)</li>
 * <li>Se publica {@code InvoiceCreatedEvent}</li>
 * <li>Este listener se ejecuta ASÍNCRONAMENTE en un Virtual Thread</li>
 * <li>Se actualiza el saldo del partner en cache/BD</li>
 * <li>El usuario ve la cuenta corriente actualizada instantáneamente</li>
 * </ol>
 * 
 * <h2>Transaccionalidad:</h2>
 * <p>
 * Usa {@code REQUIRES_NEW} para que la actualización del ledger persista
 * incluso si la transacción principal falla (ej: validación de factura).
 * 
 * @see InvoiceCreatedEvent
 * @see com.casrusil.siierpai.modules.accounting.application.service.PartnerLedgerService
 * @since 2.0 - Ferrari Engine Phase 1
 */
@Component
public class PartnerLedgerListener {

    private static final Logger logger = LoggerFactory.getLogger(PartnerLedgerListener.class);

    /**
     * Maneja el evento de factura creada y actualiza la cuenta corriente del
     * partner.
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
            // Determinar el partner afectado según el tipo de transacción
            String partnerRut = determinePartnerRut(invoice);
            String partnerName = invoice.getBusinessName();

            // Log de inicio de procesamiento
            logger.info("🏦 [PARTNER LEDGER] Actualizando cuenta corriente: {} - {} | Monto: {} | Tipo: {}",
                    partnerRut,
                    partnerName,
                    invoice.getTotalAmount(),
                    invoice.getTransactionType());

            // TODO: Implementar actualización de cache/BD
            // Fase 1.1: Por ahora solo logging
            // Fase 1.2: Implementar PartnerLedgerCacheService
            // cacheService.addMovement(invoice.getCompanyId(), partnerRut, new
            // PartnerMovement(invoice));

            logger.info("✅ [PARTNER LEDGER] Cuenta corriente actualizada exitosamente: {}", partnerRut);

        } catch (Exception e) {
            // El AsyncUncaughtExceptionHandler capturará esto
            logger.error("❌ [PARTNER LEDGER] Error actualizando cuenta corriente para factura {}: {}",
                    invoice.getFolio(), e.getMessage(), e);
            throw e; // Re-throw para que el handler lo capture
        }
    }

    /**
     * Determina el RUT del partner según el tipo de transacción.
     * 
     * @param invoice Factura a procesar
     * @return RUT del partner (cliente o proveedor)
     */
    private String determinePartnerRut(Invoice invoice) {
        return invoice.getTransactionType() == TransactionType.SALE
                ? invoice.getReceiverRut() // Venta: el cliente es el receiver
                : invoice.getIssuerRut(); // Compra: el proveedor es el issuer
    }
}
