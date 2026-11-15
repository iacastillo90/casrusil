package com.casrusil.siierpai.modules.accounting.application.listener;

import com.casrusil.siierpai.modules.accounting.domain.model.AccountingEntry;
import com.casrusil.siierpai.modules.accounting.domain.service.AccountingEntryService;
import com.casrusil.siierpai.modules.invoicing.domain.event.InvoiceCreatedEvent;
import com.casrusil.siierpai.modules.invoicing.domain.model.Invoice;
import com.casrusil.siierpai.modules.invoicing.domain.model.InvoiceLine;
import com.casrusil.siierpai.modules.invoicing.domain.model.InvoiceType;
import com.casrusil.siierpai.modules.invoicing.domain.model.TransactionType;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class InvoiceAccountingListenerTest {

    @InjectMocks
    private InvoiceAccountingListener invoiceAccountingListener;

    @Mock
    private AccountingEntryService accountingEntryService;

    @Mock
    private com.casrusil.siierpai.modules.accounting.application.service.LearningService learningService;

    @Test
    void handle_ShouldCreateAndSaveAccountingEntry() {
        // Given
        CompanyId companyId = CompanyId.random();
        Invoice invoice = Invoice.create(
               companyId,
                InvoiceType.FACTURA_ELECTRONICA,
                123L,
                "76.123.456-7",
                "76987654-3",
                LocalDate.now(),
                new BigDecimal("1000"),
                new BigDecimal("190"),
                new BigDecimal("1190"),
                Collections.emptyList());
        InvoiceCreatedEvent event = new InvoiceCreatedEvent(invoice);

        // When
        invoiceAccountingListener.handle(event);

        // Then
        ArgumentCaptor<AccountingEntry> entryCaptor = ArgumentCaptor.forClass(AccountingEntry.class);
        verify(accountingEntryService).recordEntry(entryCaptor.capture());

        AccountingEntry savedEntry = entryCaptor.getValue();
        assertEquals(companyId, savedEntry.getCompanyId());
        assertEquals("INVOICE", savedEntry.getReferenceType());
        assertEquals(invoice.getId().toString(), savedEntry.getReferenceId());
        assertEquals(3, savedEntry.getLines().size()); // Debit Client, Credit Sales, Credit VAT
    }
}
