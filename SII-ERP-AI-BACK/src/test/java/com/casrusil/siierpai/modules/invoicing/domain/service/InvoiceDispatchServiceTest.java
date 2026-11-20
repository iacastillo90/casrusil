package com.casrusil.siierpai.modules.invoicing.domain.service;

import com.casrusil.siierpai.modules.integration_sii.domain.port.out.CafRepository;
import com.casrusil.siierpai.modules.integration_sii.infrastructure.barcode.Pdf417Generator;
import com.casrusil.siierpai.modules.integration_sii.infrastructure.crypto.TedGenerator;
import com.casrusil.siierpai.modules.invoicing.application.service.InvoiceAuditTrailService;
import com.casrusil.siierpai.modules.invoicing.domain.model.Invoice;
import com.casrusil.siierpai.modules.invoicing.domain.model.InvoiceType;
import com.casrusil.siierpai.modules.invoicing.domain.port.out.InvoiceRepository;
import com.casrusil.siierpai.modules.invoicing.infrastructure.mail.InvoiceEmailSender;
import com.casrusil.siierpai.modules.invoicing.infrastructure.pdf.InvoicePdfGenerator;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import com.casrusil.siierpai.shared.infrastructure.context.CompanyContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * B-12: el despacho por email no puede filtrar facturas de otra empresa ni
 * enviar a direcciones inválidas.
 */
class InvoiceDispatchServiceTest {

    private InvoiceRepository invoiceRepository;
    private InvoiceEmailSender emailSender;
    private InvoiceDispatchService service;

    @BeforeEach
    void setUp() {
        invoiceRepository = mock(InvoiceRepository.class);
        emailSender = mock(InvoiceEmailSender.class);
        service = new InvoiceDispatchService(
                invoiceRepository,
                mock(CafRepository.class),
                mock(TedGenerator.class),
                mock(Pdf417Generator.class),
                mock(InvoicePdfGenerator.class),
                emailSender,
                mock(InvoiceAuditTrailService.class));
    }

    @Test
    void dispatchInvoice_shouldRejectInvoiceFromAnotherCompany() {
        CompanyId owner = CompanyId.random();
        CompanyId attacker = CompanyId.random();
        UUID invoiceId = UUID.randomUUID();
        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice(owner, invoiceId)));

        assertThrows(AccessDeniedException.class, () -> CompanyContext.runInCompanyContext(attacker,
                () -> service.dispatchInvoice(invoiceId, "cliente@test.cl")));

        verify(emailSender, never()).sendInvoiceEmail(
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void dispatchInvoice_shouldRejectInvalidRecipient() {
        CompanyId owner = CompanyId.random();
        UUID invoiceId = UUID.randomUUID();
        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice(owner, invoiceId)));

        assertThrows(IllegalArgumentException.class, () -> CompanyContext.runInCompanyContext(owner,
                () -> service.dispatchInvoice(invoiceId, "no-es-un-email")));
    }

    private Invoice invoice(CompanyId companyId, UUID id) {
        return new Invoice(
                id,
                companyId,
                InvoiceType.FACTURA_ELECTRONICA,
                10L,
                "76123456-7",
                "76987654-3",
                "Emisor SpA",
                LocalDate.now(),
                LocalDate.now(),
                new BigDecimal("1000"),
                new BigDecimal("190"),
                new BigDecimal("1190"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                Invoice.ORIGIN_MANUAL,
                com.casrusil.siierpai.modules.invoicing.domain.model.TransactionType.SALE,
                com.casrusil.siierpai.modules.invoicing.domain.model.PaymentStatus.PENDING,
                Collections.emptyList(),
                "CLP");
    }
}
