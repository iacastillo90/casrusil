package com.casrusil.siierpai.modules.invoicing.application.service;

import com.casrusil.siierpai.modules.invoicing.domain.event.InvoiceCreatedEvent;
import com.casrusil.siierpai.modules.invoicing.domain.model.Invoice;
import com.casrusil.siierpai.modules.invoicing.domain.model.InvoiceType;
import com.casrusil.siierpai.modules.invoicing.domain.port.out.InvoiceRepository;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import com.casrusil.siierpai.shared.infrastructure.context.CompanyContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InvoiceManagementServiceTest {

    private StubInvoiceRepository invoiceRepository;
    private StubEventPublisher eventPublisher;
    private InvoiceManagementService service;

    @BeforeEach
    void setUp() {
        invoiceRepository = new StubInvoiceRepository();
        eventPublisher = new StubEventPublisher();
        service = new InvoiceManagementService(invoiceRepository, eventPublisher);
    }

    @Test
    void createInvoice_shouldPublishEvent() {
        // Given
        CompanyId companies = CompanyId.random();
        Invoice invoice = Invoice.create(
                companies,
                InvoiceType.FACTURA_ELECTRONICA,
                123L,
                "76.123.456-7",
                "12.345.678-9",
                LocalDate.now(),
                new BigDecimal("1000"),
                new BigDecimal("190"),
                new BigDecimal("1190"),
                Collections.emptyList());

        // When
        CompanyContext.runInCompanyContext(companies, () -> {
            service.createInvoice(invoice);
        });

        // Then
        assertTrue(invoiceRepository.saved);
        assertEquals(1, eventPublisher.events.size());
        assertTrue(eventPublisher.events.get(0) instanceof InvoiceCreatedEvent);
        assertEquals(invoice, ((InvoiceCreatedEvent) eventPublisher.events.get(0)).invoice());
    }

    static class StubInvoiceRepository implements InvoiceRepository {
        boolean saved = false;

        @Override
        public Invoice save(Invoice invoice) {
            saved = true;
            return invoice;
        }

        @Override
        public Optional<Invoice> findById(UUID id) {
            return Optional.empty();
        }

        @Override
        public List<Invoice> findByCompanyId(CompanyId companies) {
            return Collections.emptyList();
        }

        @Override
        public boolean existsByCompanyIdAndTypeCodeAndFolioAndIssuerRut(CompanyId companies, Integer typeCode, Long folio, String issuerRut) {
            return false;
        }

        @Override
        public Long findMaxFolioByCompanyIdAndTypeCode(CompanyId companies, int typeCode) {
            return null;
        }

        @Override
        public void deleteInPeriod(CompanyId companies, LocalDate start, LocalDate end) {
            // No-op for test stub
        }

        @Override
        public List<Invoice> findByCompanyIdAndDateBetween(CompanyId companies, LocalDate start, LocalDate end) {
            return Collections.emptyList();
        }
    }

    static class StubEventPublisher implements ApplicationEventPublisher {
        List<Object> events = new ArrayList<>();

        @Override
        public void publishEvent(Object event) {
            events.add(event);
        }
    }
}