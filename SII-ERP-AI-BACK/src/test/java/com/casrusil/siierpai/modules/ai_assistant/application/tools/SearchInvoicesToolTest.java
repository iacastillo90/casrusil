package com.casrusil.siierpai.modules.ai_assistant.application.tools;

import com.casrusil.siierpai.modules.invoicing.domain.model.Invoice;
import com.casrusil.siierpai.modules.invoicing.domain.model.InvoiceType;
import com.casrusil.siierpai.modules.invoicing.domain.port.in.SearchInvoicesUseCase;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import com.casrusil.siierpai.shared.infrastructure.context.CompanyContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchInvoicesToolTest {

    @Mock
    private SearchInvoicesUseCase searchInvoicesUseCase;

    private SearchInvoicesTool searchInvoicesTool;

    @BeforeEach
    void setUp() {
        com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        searchInvoicesTool = new SearchInvoicesTool(searchInvoicesUseCase, objectMapper);
    }

    @Test
    void shouldReturnInvoiceList() throws Exception {
        // Given
        CompanyId companyId = new CompanyId(UUID.randomUUID());
        Invoice invoice = Invoice.create(
                companyId,
                InvoiceType.FACTURA_ELECTRONICA,
                123L,
                "76123456-7",
                "76987654-3",
                LocalDate.now(),
                new BigDecimal("1000"),
                new BigDecimal("190"),
                new BigDecimal("1190"),
                Collections.emptyList());

        when(searchInvoicesUseCase.getInvoicesByCompany(any(CompanyId.class)))
                .thenReturn(List.of(invoice));

        // When
        String result = ScopedValue.where(CompanyContext.COMPANY_ID, companyId)
                .call(() -> searchInvoicesTool.execute(""));

        // Then
        assertNotNull(result);
        assertTrue(result.contains("\"summary\":\"Found 1 invoices.\""), result);
        assertTrue(result.contains("\"folio\":123"), result);
        assertTrue(result.contains("76123456-7"), result);
        assertTrue(result.contains("\"totalAmount\":1190"), result);
    }

    @Test
    void shouldReturnNoInvoicesMessage() throws Exception {
        // Given
        CompanyId companyId = new CompanyId(UUID.randomUUID());
        when(searchInvoicesUseCase.getInvoicesByCompany(any(CompanyId.class)))
                .thenReturn(Collections.emptyList());

        // When
        String result = ScopedValue.where(CompanyContext.COMPANY_ID, companyId)
                .call(() -> searchInvoicesTool.execute(""));

        // Then
        assertEquals("[]", result);
    }

    @Test
    void shouldHaveCorrectMetadata() {
        assertEquals("search_invoices", searchInvoicesTool.name());
        assertNotNull(searchInvoicesTool.description());
        assertTrue(searchInvoicesTool.description().contains("invoices"));
    }
}
