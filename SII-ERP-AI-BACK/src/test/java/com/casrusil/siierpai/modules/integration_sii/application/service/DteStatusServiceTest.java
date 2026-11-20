package com.casrusil.siierpai.modules.integration_sii.application.service;

import com.casrusil.siierpai.modules.integration_sii.domain.model.SiiDteQueryResult;
import com.casrusil.siierpai.modules.integration_sii.domain.model.SiiToken;
import com.casrusil.siierpai.modules.integration_sii.domain.port.out.SiiDteStatusClient;
import com.casrusil.siierpai.modules.integration_sii.domain.port.out.SiiTokenRepository;
import com.casrusil.siierpai.modules.integration_sii.domain.service.SiiDteStatusMapper;
import com.casrusil.siierpai.modules.invoicing.application.service.InvoiceAuditTrailService;
import com.casrusil.siierpai.modules.invoicing.domain.event.InvoiceSiiAcceptedEvent;
import com.casrusil.siierpai.modules.invoicing.domain.model.Invoice;
import com.casrusil.siierpai.modules.invoicing.domain.model.InvoiceType;
import com.casrusil.siierpai.modules.invoicing.domain.model.TransactionType;
import com.casrusil.siierpai.modules.invoicing.domain.port.out.InvoiceRepository;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * B-02 + B-13: el estado consultado al SII actualiza la factura, deja auditoría
 * y sólo publica el evento de aceptación cuando corresponde.
 */
class DteStatusServiceTest {

    private InvoiceRepository invoiceRepository;
    private ApplicationEventPublisher eventPublisher;
    private SiiDteStatusClient statusClient;
    private SiiTokenRepository tokenRepository;
    private InvoiceAuditTrailService auditTrailService;
    private DteStatusService service;
    private CompanyId companyId;

    @BeforeEach
    void setUp() {
        invoiceRepository = mock(InvoiceRepository.class);
        eventPublisher = mock(ApplicationEventPublisher.class);
        statusClient = mock(SiiDteStatusClient.class);
        tokenRepository = mock(SiiTokenRepository.class);
        auditTrailService = mock(InvoiceAuditTrailService.class);
        companyId = CompanyId.random();

        service = new DteStatusService(invoiceRepository, auditTrailService, eventPublisher, statusClient,
                tokenRepository, new SiiDteStatusMapper("EPR,DOK", "", "RCT,RFT,RCH"), 5, "EPR");

        when(tokenRepository.findByCompanyId(any()))
                .thenReturn(Optional.of(new SiiToken("token", Instant.now().plusSeconds(3600))));
    }

    @Test
    void acceptedDteShouldUpdateStatusAuditAndPublishEvent() {
        Invoice invoice = sentInvoice();
        when(statusClient.queryEnvio(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(new SiiDteQueryResult("000", "OK", "EPR", "Envio procesado", "<raw/>"));
        when(statusClient.queryDte(anyString(), anyString(), anyString(), anyString(), anyString(),
                anyString(), anyString(), any(Integer.class), any(Long.class), any(LocalDate.class),
                any(BigDecimal.class)))
                .thenReturn(new SiiDteQueryResult("000", "OK", "DOK", "Documento aceptado", "<raw/>"));

        assertTrue(service.refresh(invoice));

        assertEquals("ACEPTADO", invoice.getDteStatus());
        verify(invoiceRepository).save(invoice);
        verify(auditTrailService).record(any(), any(), anyString(), anyString());
        ArgumentCaptor<Object> event = ArgumentCaptor.forClass(Object.class);
        verify(eventPublisher).publishEvent(event.capture());
        assertTrue(event.getValue() instanceof InvoiceSiiAcceptedEvent);
    }

    @Test
    void rejectedDteShouldNotPublishAccountingEvent() {
        Invoice invoice = sentInvoice();
        when(statusClient.queryEnvio(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(new SiiDteQueryResult("000", "OK", "RCH", "Rechazado", "<raw/>"));

        assertTrue(service.refresh(invoice));

        assertEquals("RECHAZADO", invoice.getDteStatus());
        verify(eventPublisher, never()).publishEvent(any(Object.class));
    }

    @Test
    void pendingDteShouldRemainSentAndNotPublish() {
        Invoice invoice = sentInvoice();
        when(statusClient.queryEnvio(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(new SiiDteQueryResult("000", "OK", "XYZ", "Desconocido", "<raw/>"));

        assertFalse(service.refresh(invoice));

        assertEquals(Invoice.DTE_STATUS_SENT, invoice.getDteStatus());
        verify(eventPublisher, never()).publishEvent(any(Object.class));
    }

    @Test
    void invoiceNotSentShouldBeIgnored() {
        Invoice invoice = Invoice.create(companyId, InvoiceType.FACTURA_ELECTRONICA, 10L, "76123456-7",
                "76987654-3", "Emisor SpA", LocalDate.now(), new BigDecimal("1000"), new BigDecimal("190"),
                new BigDecimal("1190"), BigDecimal.ZERO, BigDecimal.ZERO, Invoice.ORIGIN_MANUAL,
                TransactionType.SALE, Collections.emptyList());

        assertFalse(service.refresh(invoice));
        verify(invoiceRepository, never()).save(any());
    }

    @Test
    void splitRutShouldSeparateBodyAndDv() {
        assertEquals("76123456", DteStatusService.splitRut("76123456-7")[0]);
        assertEquals("7", DteStatusService.splitRut("76123456-7")[1]);
        assertEquals("76123456", DteStatusService.splitRut("76.123.456-7")[0]);
    }

    private Invoice sentInvoice() {
        Invoice invoice = Invoice.create(companyId, InvoiceType.FACTURA_ELECTRONICA, 10L, "76123456-7",
                "76987654-3", "Emisor SpA", LocalDate.now(), new BigDecimal("1000"), new BigDecimal("190"),
                new BigDecimal("1190"), BigDecimal.ZERO, BigDecimal.ZERO, Invoice.ORIGIN_MANUAL,
                TransactionType.SALE, Collections.emptyList());
        invoice.markAsSent("track-1", LocalDateTime.now());
        return invoice;
    }
}
