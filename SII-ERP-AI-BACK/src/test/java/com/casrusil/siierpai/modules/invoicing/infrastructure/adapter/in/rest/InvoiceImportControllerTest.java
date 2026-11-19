package com.casrusil.siierpai.modules.invoicing.infrastructure.adapter.in.rest;

import com.casrusil.siierpai.modules.invoicing.domain.model.Invoice;
import com.casrusil.siierpai.modules.invoicing.domain.model.InvoiceType;
import com.casrusil.siierpai.modules.invoicing.domain.port.in.CreateInvoiceUseCase;
import com.casrusil.siierpai.modules.sso.domain.port.out.CompanyRepository;
import com.casrusil.siierpai.modules.fees.application.service.FeeReceiptService;
import com.casrusil.siierpai.modules.sso.domain.model.Company;
import com.casrusil.siierpai.modules.partners.domain.port.out.PartnerRepository;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class InvoiceImportControllerTest {

    private MockMvc mockMvc;
    private StubCreateInvoiceUseCase createInvoiceUseCase;
    private com.casrusil.siierpai.modules.accounting.domain.port.out.AccountRepository accountRepository;
    private PartnerRepository partnerRepository;

    @BeforeEach
    void setUp() {
        createInvoiceUseCase = new StubCreateInvoiceUseCase();
        accountRepository = org.mockito.Mockito.mock(com.casrusil.siierpai.modules.accounting.domain.port.out.AccountRepository.class);
        partnerRepository = org.mockito.Mockito.mock(PartnerRepository.class);

        InvoiceImportController controller = new InvoiceImportController(createInvoiceUseCase, accountRepository, partnerRepository);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void importSiiCsv_shouldParseAndCreateInvoices() throws Exception {
        byte[] csvContent;
        try (var is = getClass().getResourceAsStream("/fixtures/import-sale.csv")) {
            assertNotNull(is, "No se encontró el fixture /fixtures/import-sale.csv");
            csvContent = is.readAllBytes();
        }

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "import-sale.csv",
                MediaType.TEXT_PLAIN_VALUE,
                csvContent);

        UUID companyId = UUID.randomUUID();

        mockMvc.perform(multipart("/api/v1/invoices/import/sii-csv")
                .file(file)
                .param("companyId", companyId.toString())
                .param("bookType", "SALE"))
                .andExpect(status().isOk());

        assertEquals(1, createInvoiceUseCase.createdInvoices.size());
        assertEquals(Invoice.ORIGIN_MANUAL, createInvoiceUseCase.createdInvoices.get(0).getOrigin());
    }

    static class StubCreateInvoiceUseCase implements CreateInvoiceUseCase {
        List<Invoice> createdInvoices = new ArrayList<>();

        @Override
        public Invoice createInvoice(Invoice invoice) {
            createdInvoices.add(invoice);
            return invoice;
        }
    }
}