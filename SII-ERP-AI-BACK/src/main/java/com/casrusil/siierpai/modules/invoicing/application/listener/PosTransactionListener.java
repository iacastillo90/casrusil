package com.casrusil.siierpai.modules.invoicing.application.listener;

import com.casrusil.siierpai.modules.invoicing.domain.model.Invoice;
import com.casrusil.siierpai.modules.invoicing.domain.model.InvoiceType;
import com.casrusil.siierpai.modules.invoicing.domain.port.in.CreateInvoiceUseCase;
import com.casrusil.siierpai.modules.pos.domain.event.PosTransactionCompletedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Component
public class PosTransactionListener {

    private static final Logger logger = LoggerFactory.getLogger(PosTransactionListener.class);
    private final CreateInvoiceUseCase createInvoiceUseCase;

    public PosTransactionListener(CreateInvoiceUseCase createInvoiceUseCase) {
        this.createInvoiceUseCase = createInvoiceUseCase;
    }

    @EventListener
    public void onPosTransactionCompleted(PosTransactionCompletedEvent event) {
        logger.info("Handling PosTransactionCompletedEvent for transaction: {}", event.getTransaction().getId());
        
        try {
            // Mapping POS Transaction to a Boleta Electrónica (DTE 39)
            BigDecimal totalAmount = event.getTransaction().getTotalAmount();
            BigDecimal netAmount = totalAmount.divide(new BigDecimal("1.19"), 0, RoundingMode.HALF_UP);
            BigDecimal vatAmount = totalAmount.subtract(netAmount);

            Invoice invoice = Invoice.create(
                event.getTransaction().getCompanyId(),
                InvoiceType.BOLETA_ELECTRONICA,
                null, // El folio se asigna server-side desde el CAF correspondiente
                "66666666-6", // Generic public RUT for B2C sales
                "Cliente Final",
                LocalDate.now(),
                netAmount,
                vatAmount,
                totalAmount,
                java.util.Collections.emptyList()
            );

            createInvoiceUseCase.createInvoice(invoice);
            logger.info("Successfully generated Boleta for POS transaction: {}", event.getTransaction().getId());
        } catch (Exception e) {
            logger.error("Error generating Boleta for POS transaction: {}", event.getTransaction().getId(), e);
        }
    }
}
