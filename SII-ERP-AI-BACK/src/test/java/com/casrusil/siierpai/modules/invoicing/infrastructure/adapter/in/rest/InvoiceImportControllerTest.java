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
    private com.casrusil.siierpai.modules.sso.domain.port.out.CompanyRepository companyRepository;
    private final com.casrusil.siierpai.shared.domain.valueobject.CompanyId company =
            com.casrusil.siierpai.shared.domain.valueobject.CompanyId.random();
    private static final String OWN_RUT = "76123456-7";

    @BeforeEach
    void setUp() {
        createInvoiceUseCase = new StubCreateInvoiceUseCase();
        accountRepository = org.mockito.Mockito.mock(com.casrusil.siierpai.modules.accounting.domain.port.out.AccountRepository.class);
        partnerRepository = org.mockito.Mockito.mock(PartnerRepository.class);

        companyRepository = org.mockito.Mockito.mock(com.casrusil.siierpai.modules.sso.domain.port.out.CompanyRepository.class);
        org.mockito.Mockito.when(companyRepository.findById(company)).thenReturn(java.util.Optional.of(
                new com.casrusil.siierpai.modules.sso.domain.model.Company(company, OWN_RUT, "Empresa Propia SpA",
                        "admin@propia.cl", true, java.time.Instant.now(), null, null, null, null, true, null, null,
                        null)));

        InvoiceImportController controller = new InvoiceImportController(createInvoiceUseCase, accountRepository,
                partnerRepository, companyRepository);
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

        asCompany(company, multipart("/api/v1/invoices/import/sii-csv")
                .file(file)
                .param("bookType", "SALE"))
                .andExpect(status().isOk());

        assertEquals(1, createInvoiceUseCase.createdInvoices.size());
        // F08: el emisor de una venta es la empresa autenticada, con su RUT real.
        assertEquals(OWN_RUT, createInvoiceUseCase.createdInvoices.get(0).getIssuerRut());
        assertEquals(company, createInvoiceUseCase.createdInvoices.get(0).getCompanyId());
        assertEquals(Invoice.ORIGIN_MANUAL, createInvoiceUseCase.createdInvoices.get(0).getOrigin());
        assertEquals("Cliente Test SpA", createInvoiceUseCase.createdInvoices.get(0).getReceiverName());
    }

    /**
     * Deuda técnica: un monto mal formado se convertía en 0 en silencio y la
     * factura se importaba con montos en cero. Ahora la fila cuenta como error y
     * no se crea la factura.
     */
    @Test
    void importSiiCsv_montoMalFormado_reportaErrorEnLaFilaYNoCreaFactura() throws Exception {
        String csv = "Tipo Doc;Folio;Rut cliente;Razon Social;Fecha Docto;Monto Neto;Monto IVA;Monto Total\n"
                + "33;1001;12345678-9;Cliente Test SpA;27/10/2023;100000;19000;119000\n"
                + "33;1002;12345678-9;Cliente Test SpA;27/10/2023;CIEN MIL;19000;119000\n";
        MockMultipartFile file = new MockMultipartFile("file", "import.csv", MediaType.TEXT_PLAIN_VALUE,
                csv.getBytes(java.nio.charset.StandardCharsets.ISO_8859_1));

        asCompany(company, multipart("/api/v1/invoices/import/sii-csv")
                .file(file)
                .param("bookType", "SALE"))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .jsonPath("$.successCount").value(1))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .jsonPath("$.errorCount").value(1))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Monto inválido")));

        assertEquals(1, createInvoiceUseCase.createdInvoices.size());
        assertEquals(1001L, createInvoiceUseCase.createdInvoices.get(0).getFolio());
    }

    @Test
    void parseAmount_formatoChilenoConMilesYDecimales() throws Exception {
        String csv = "Monto Neto;Monto IVA\n1.234.567;\n";
        try (var parser = org.apache.commons.csv.CSVParser.parse(csv, org.apache.commons.csv.CSVFormat.Builder
                .create().setDelimiter(';').setHeader().setSkipHeaderRecord(true).build())) {
            var record = parser.getRecords().get(0);
            InvoiceImportController controller = new InvoiceImportController(createInvoiceUseCase, accountRepository,
                    partnerRepository, companyRepository);
            assertEquals(new BigDecimal("1234567"), controller.parseAmount(record, "Monto Neto"));
            // Columna vacía o ausente sigue valiendo cero (comportamiento previo).
            assertEquals(BigDecimal.ZERO, controller.parseAmount(record, "Monto IVA"));
            assertEquals(BigDecimal.ZERO, controller.parseAmount(record, "Monto Exento"));
        }
    }

    /**
     * R1: aunque el cliente envíe companyId de otra empresa, la factura se crea en la
     * empresa del JWT. El parámetro ya no existe en el controller y se ignora.
     */
    @Test
    void importSiiCsv_companyIdDeOtraEmpresaEnLaPeticion_seIgnoraYUsaLaDelToken() throws Exception {
        String csv = "Tipo Doc;Folio;RUT Proveedor;Razon Social;Fecha Docto;Monto Neto;Monto IVA;Monto Total\n"
                + "33;9001;11111111-1;Proveedor SpA;27/10/2023;100000;19000;119000\n";
        MockMultipartFile file = new MockMultipartFile("file", "compras.csv", MediaType.TEXT_PLAIN_VALUE,
                csv.getBytes(java.nio.charset.StandardCharsets.ISO_8859_1));

        asCompany(company, multipart("/api/v1/invoices/import/sii-csv")
                .file(file)
                .param("companyId", UUID.randomUUID().toString())
                .param("bookType", "COMPRA"))
                .andExpect(status().isOk());

        assertEquals(1, createInvoiceUseCase.createdInvoices.size());
        Invoice created = createInvoiceUseCase.createdInvoices.get(0);
        assertEquals(company, created.getCompanyId());
        // F08: en compras el receptor es la empresa propia con su RUT real (no "EMPRESA_PROPIA").
        assertEquals(OWN_RUT, created.getReceiverRut());
    }

    @Test
    void importSiiCsv_empresaSinRut_responde422SinCrearFacturas() throws Exception {
        var otherCompany = com.casrusil.siierpai.shared.domain.valueobject.CompanyId.random();
        org.mockito.Mockito.when(companyRepository.findById(otherCompany)).thenReturn(java.util.Optional.empty());
        MockMultipartFile file = new MockMultipartFile("file", "x.csv", MediaType.TEXT_PLAIN_VALUE,
                "Tipo Doc;Folio\n33;1\n".getBytes(java.nio.charset.StandardCharsets.ISO_8859_1));

        asCompany(otherCompany, multipart("/api/v1/invoices/import/sii-csv").file(file).param("bookType", "SALE"))
                .andExpect(status().isUnprocessableEntity());
        assertEquals(0, createInvoiceUseCase.createdInvoices.size());
    }

    private org.springframework.test.web.servlet.ResultActions asCompany(
            com.casrusil.siierpai.shared.domain.valueobject.CompanyId companyId,
            org.springframework.test.web.servlet.RequestBuilder request) throws Exception {
        return java.lang.ScopedValue
                .where(com.casrusil.siierpai.shared.infrastructure.context.CompanyContext.COMPANY_ID, companyId)
                .call(() -> mockMvc.perform(request));
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