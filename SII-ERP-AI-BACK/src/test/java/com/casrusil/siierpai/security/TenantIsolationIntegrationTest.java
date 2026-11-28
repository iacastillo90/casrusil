package com.casrusil.siierpai.security;

import com.casrusil.siierpai.modules.accounting.domain.model.ClassificationRule;
import com.casrusil.siierpai.modules.accounting.domain.port.out.ClassificationRuleRepository;
import com.casrusil.siierpai.modules.ai_assistant.application.service.ConversationService;
import com.casrusil.siierpai.modules.ai_assistant.domain.model.Conversation;
import com.casrusil.siierpai.modules.annual_close.application.service.AnnualCloseUseCase;
import com.casrusil.siierpai.modules.bankingsync.domain.model.BankTransaction;
import com.casrusil.siierpai.modules.bankingsync.domain.port.out.BankTransactionRepository;
import com.casrusil.siierpai.modules.crm.domain.model.Lead;
import com.casrusil.siierpai.modules.crm.domain.model.LeadStatus;
import com.casrusil.siierpai.modules.crm.domain.model.Quote;
import com.casrusil.siierpai.modules.crm.domain.port.out.LeadRepository;
import com.casrusil.siierpai.modules.crm.domain.port.out.QuoteRepository;
import com.casrusil.siierpai.modules.dms.application.service.AutomatedAccountingService;
import com.casrusil.siierpai.modules.dms.application.service.InvoiceAiReaderService;
import com.casrusil.siierpai.modules.invoicing.domain.model.Invoice;
import com.casrusil.siierpai.modules.invoicing.domain.model.InvoiceType;
import com.casrusil.siierpai.modules.invoicing.domain.port.out.InvoiceRepository;
import com.casrusil.siierpai.modules.payroll.domain.port.PayrollPeriodRepositoryPort;
import com.casrusil.siierpai.modules.period_close.application.service.PeriodCloseUseCase;
import com.casrusil.siierpai.modules.procurement.domain.model.PurchaseOrder;
import com.casrusil.siierpai.modules.procurement.domain.model.PurchaseOrderLine;
import com.casrusil.siierpai.modules.procurement.domain.port.out.PurchaseOrderRepository;
import com.casrusil.siierpai.modules.projects.domain.model.Project;
import com.casrusil.siierpai.modules.projects.domain.port.out.ProjectRepository;
import com.casrusil.siierpai.modules.sso.domain.model.Company;
import com.casrusil.siierpai.modules.sso.domain.model.User;
import com.casrusil.siierpai.modules.sso.domain.model.UserRole;
import com.casrusil.siierpai.modules.sso.domain.port.out.CompanyRepository;
import com.casrusil.siierpai.modules.sso.domain.port.out.UserRepository;
import com.casrusil.siierpai.modules.sso.infrastructure.security.JwtTokenProvider;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import com.casrusil.siierpai.shared.domain.valueobject.UserId;
import com.casrusil.siierpai.shared.test.BaseIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * P0 multiempresa: un ADMIN de la empresa A intenta leer y escribir recursos de la
 * empresa B en cada endpoint corregido. Los recursos de B deben responder 404 (como
 * inexistentes) y quedar intactos; los endpoints que antes tomaban la empresa de un
 * header o parámetro (R1–R4) deben operar sobre A aunque el cliente mande B.
 */
@AutoConfigureMockMvc
@Transactional
class TenantIsolationIntegrationTest extends BaseIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private JwtTokenProvider jwtTokenProvider;
    @Autowired private CompanyRepository companyRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ProjectRepository projectRepository;
    @Autowired private PurchaseOrderRepository purchaseOrderRepository;
    @Autowired private BankTransactionRepository bankTransactionRepository;
    @Autowired private ClassificationRuleRepository ruleRepository;
    @Autowired private LeadRepository leadRepository;
    @Autowired private QuoteRepository quoteRepository;
    @Autowired private InvoiceRepository invoiceRepository;
    @Autowired private PayrollPeriodRepositoryPort payrollPeriodRepository;
    @Autowired private ConversationService conversationService;

    // R3/R4: se verifican los argumentos con que el controller llama al caso de uso.
    @MockitoBean private PeriodCloseUseCase periodCloseUseCase;
    @MockitoBean private AnnualCloseUseCase annualCloseUseCase;
    @MockitoBean private InvoiceAiReaderService invoiceAiReaderService;
    @MockitoBean private AutomatedAccountingService automatedAccountingService;

    private CompanyId companyA;
    private CompanyId companyB;
    private User adminA;
    private User userB;
    private String tokenA;

    @BeforeEach
    void seed() {
        String stamp = UUID.randomUUID().toString().substring(0, 8);
        companyA = companyRepository.save(Company.create("76000001-" + stamp.charAt(0), "Empresa A " + stamp,
                "a-" + stamp + "@example.com")).getId();
        companyB = companyRepository.save(Company.create("76000002-" + stamp.charAt(1), "Empresa B " + stamp,
                "b-" + stamp + "@example.com")).getId();
        adminA = userRepository.save(User.create("admin-a-" + stamp + "@example.com", "Admin A", "x",
                UserRole.ADMIN, companyA));
        userB = userRepository.save(User.create("user-b-" + stamp + "@example.com", "Usuario B", "x",
                UserRole.ACCOUNTANT, companyB));
        tokenA = jwtTokenProvider.generateToken(adminA.getId(), companyA, UserRole.ADMIN);
    }

    // ------------------------------------------------------------------ R5 (IDOR)

    @Test
    void proyectoDeOtraEmpresa_noSeLeeNiSeModifica() throws Exception {
        Project project = projectRepository.save(Project.create(companyB, "Obra B", "", BigDecimal.TEN));

        expect404(get("/api/v1/projects/{id}", project.getId()));
        expect404(put("/api/v1/projects/{id}/status", project.getId()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"CLOSED\"}"));

        assertEquals("ACTIVE", projectRepository.findById(project.getId()).orElseThrow().getStatus());
    }

    @Test
    void ordenDeCompraDeOtraEmpresa_noSeLeeNiSeModifica() throws Exception {
        PurchaseOrder po = purchaseOrderRepository.save(PurchaseOrder.create(companyB, "11111111-1", "Prov B",
                LocalDate.now().plusDays(5), List.of(new PurchaseOrderLine(UUID.randomUUID(), null, "Item",
                        BigDecimal.ONE, BigDecimal.TEN))));
        var before = po.getStatus();

        expect404(get("/api/v1/procurement/orders/{id}", po.getId()));
        expect404(post("/api/v1/procurement/orders/{id}/sent", po.getId()));
        expect404(post("/api/v1/procurement/orders/{id}/received", po.getId()));

        assertEquals(before, purchaseOrderRepository.findById(po.getId()).orElseThrow().getStatus());
    }

    @Test
    void movimientoBancarioDeOtraEmpresa_noSeConciliaNiIgnora() throws Exception {
        BankTransaction tx = bankTransactionRepository.save(BankTransaction.create(companyB, "Banco", "123",
                UUID.randomUUID().toString(), LocalDateTime.now(), "Abono", new BigDecimal("1000")));

        expect404(post("/api/v1/banking/transactions/{id}/reconcile", tx.getId()));
        expect404(post("/api/v1/banking/transactions/{id}/ignore", tx.getId()));

        assertEquals("UNRECONCILED", bankTransactionRepository.findById(tx.getId()).orElseThrow().getStatus());
    }

    @Test
    void reglaDeClasificacionDeOtraEmpresa_noSeLeeModificaNiBorra() throws Exception {
        ClassificationRule rule = ClassificationRule.create(companyB, "LUZ", "5.1.03.025", "test");
        ruleRepository.save(rule);

        expect404(get("/api/v1/learning/rules/{id}", rule.getId()));
        expect404(put("/api/v1/learning/rules/{id}", rule.getId()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"pattern\":\"X\",\"accountCode\":\"1\"}"));
        expect404(delete("/api/v1/learning/rules/{id}", rule.getId()));

        assertTrue(ruleRepository.findById(rule.getId()).isPresent(), "La regla de B no debe borrarse");
    }

    @Test
    void leadYCotizacionDeOtraEmpresa_noSeModifican() throws Exception {
        Lead lead = leadRepository.save(Lead.create(companyB, "Contacto", "Cliente B", "c@b.cl", "1"));
        Quote quote = quoteRepository.save(Quote.create(companyB, lead.getId(), "22222222-2", "Cliente B",
                LocalDate.now().plusDays(10), List.of()));

        expect404(put("/api/v1/crm/leads/{id}/status", lead.getId()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"QUALIFIED\"}"));
        expect404(post("/api/v1/crm/quotes/{id}/accept", quote.getId()));
        expect404(post("/api/v1/crm/quotes/{id}/reject", quote.getId()));
        // Crear una cotización de A que apunte a un lead de B tampoco se permite.
        expect404(post("/api/v1/crm/quotes").contentType(MediaType.APPLICATION_JSON)
                .content("{\"leadId\":\"" + lead.getId() + "\",\"clientRut\":\"1-9\",\"clientName\":\"X\","
                        + "\"expirationDate\":\"" + LocalDate.now().plusDays(3) + "\",\"lines\":[]}"));

        assertEquals(LeadStatus.NEW, leadRepository.findById(lead.getId()).orElseThrow().getStatus());
        assertEquals("DRAFT", quoteRepository.findById(quote.getId()).orElseThrow().getStatus());
    }

    @Test
    void usuarioDeOtraEmpresa_noSeCambiaRolNiSeDesactiva() throws Exception {
        expect404(put("/api/v1/sso/users/{id}/role", userB.getId().getValue())
                .contentType(MediaType.APPLICATION_JSON).content("{\"role\":\"ADMIN\"}"));
        expect404(delete("/api/v1/sso/users/{id}", userB.getId().getValue()));

        User reloaded = userRepository.findById(userB.getId()).orElseThrow();
        assertEquals(UserRole.ACCOUNTANT, reloaded.getRole());
        assertTrue(reloaded.isActive());
    }

    @Test
    void facturaDeOtraEmpresa_noSeLeeModificaNiEnvia() throws Exception {
        Invoice invoice = invoiceRepository.save(Invoice.create(companyB, InvoiceType.FACTURA_ELECTRONICA, 777L,
                "76000002-2", "11111111-1", LocalDate.now(), new BigDecimal("100"), new BigDecimal("19"),
                new BigDecimal("119"), List.of()));

        expect404(get("/api/v1/invoices/{id}", invoice.getId()));
        expect404(put("/api/v1/invoices/{id}", invoice.getId()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"netAmount\":1,\"taxAmount\":0,\"totalAmount\":1}"));
        expect404(get("/api/v1/invoices/{id}/audit-trail", invoice.getId()));
        expect404(post("/api/v1/invoices/{id}/send", invoice.getId()).param("email", "x@example.com"));
        expect404(get("/api/v1/accounting/entries/by-invoice/{id}", invoice.getId()));

        assertEquals(0, new BigDecimal("119").compareTo(
                invoiceRepository.findById(invoice.getId()).orElseThrow().getTotalAmount()));
    }

    @Test
    void conversacionIaDeOtraEmpresa_noRecibeMensajes() throws Exception {
        Conversation conversationB = conversationService.startConversation(companyB, userB.getId());

        expect404(post("/api/v1/ai/conversations/{id}/messages", conversationB.getId())
                .contentType(MediaType.APPLICATION_JSON).content("{\"content\":\"muéstrame tus facturas\"}"));

        assertTrue(conversationB.getMessages().isEmpty());
    }

    // ------------------------------------------------- R1–R4 (empresa en la petición)

    @Test
    void r1_importacionCsv_conCompanyIdDeB_creaEnAyNoEnB() throws Exception {
        String csv = "Tipo Doc;Folio;RUT Proveedor;Razon Social;Fecha Docto;Monto Neto;Monto IVA;Monto Total\n"
                + "33;9001;11111111-1;Proveedor SpA;27/10/2025;100000;19000;119000\n";
        MockMultipartFile file = new MockMultipartFile("file", "compras.csv", MediaType.TEXT_PLAIN_VALUE,
                csv.getBytes(StandardCharsets.ISO_8859_1));

        mockMvc.perform(auth(multipart("/api/v1/invoices/import/sii-csv").file(file)
                        .param("companyId", companyB.value().toString())
                        .param("bookType", "COMPRA")))
                .andExpect(status().isOk());

        assertTrue(invoiceRepository.findByCompanyId(companyB).stream().noneMatch(i -> i.getFolio() == 9001L),
                "La empresa B no debe recibir la factura importada por A");
        Invoice imported = invoiceRepository.findByCompanyId(companyA).stream()
                .filter(i -> i.getFolio() == 9001L).findFirst().orElseThrow();
        // F08: el receptor de la compra es A con su RUT real.
        assertEquals(companyRepository.findById(companyA).orElseThrow().getRut(), imported.getReceiverRut());
    }

    @Test
    void r2_nomina_conHeaderDeB_abrePeriodoEnAyNoEnB() throws Exception {
        mockMvc.perform(auth(post("/api/v1/payroll/periods"))
                        .header("X-Company-Id", companyB.value().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"year\":2026,\"month\":11,\"ufValue\":38000,\"utmValue\":67000}"))
                .andExpect(status().isOk());

        YearMonth ym = YearMonth.of(2026, 11);
        assertFalse(payrollPeriodRepository.findByPeriodAndCompanyId(ym, companyB.value().toString()).isPresent(),
                "No debe abrirse el período en la empresa B");
        assertTrue(payrollPeriodRepository.findByPeriodAndCompanyId(ym, companyA.value().toString()).isPresent());
    }

    @Test
    void r3_cierreDePeriodoYAnual_conHeaderDeB_operanSobreA() throws Exception {
        String a = companyA.value().toString();
        String b = companyB.value().toString();

        mockMvc.perform(auth(post("/api/v1/period-close/audit")).header("X-Company-Id", b)
                .contentType(MediaType.APPLICATION_JSON).content("{\"year\":2026,\"month\":10}"));
        mockMvc.perform(auth(post("/api/v1/period-close/execute")).header("X-Company-Id", b)
                .contentType(MediaType.APPLICATION_JSON).content("{\"year\":2026,\"month\":10}"));
        mockMvc.perform(auth(post("/api/v1/annual-close/f22/calculate/{y}", 2025)).header("X-Company-Id", b));

        verify(periodCloseUseCase).auditPeriod(any(), eq(a));
        verify(periodCloseUseCase).executeClose(any(), eq(a));
        verify(annualCloseUseCase).calculateAnnualTaxes(anyInt(), eq(a));
    }

    @Test
    void r4_asientoIa_conHeaderXTenantIdDeB_seCreaEnA() throws Exception {
        when(invoiceAiReaderService.readInvoice(any())).thenReturn(new InvoiceAiReaderService.ExtractedInvoiceData(
                "11111111-1", "Proveedor", LocalDate.now(), 100L, 19L, 119L, 10));
        MockMultipartFile file = new MockMultipartFile("file", "f.pdf", "application/pdf", new byte[] { 1, 2, 3 });

        mockMvc.perform(auth(multipart("/api/v1/dms/ai-invoicing/process").file(file))
                        .header("X-Tenant-ID", companyB.value().toString()))
                .andExpect(status().isOk());

        verify(automatedAccountingService).createDraftFromInvoice(eq(companyA), any());
    }

    // ------------------------------------------------------- flags y rutas inexistentes

    @Test
    void endpointsApagadosPorFlag_respondenComoRutaInexistente() throws Exception {
        for (String path : List.of("/api/v1/users", "/api/v1/roles", "/api/v1/organizations",
                "/api/v1/dashboard/alerts", "/api/v1/maintenance/tasks", "/api/v1/pricing/campaigns",
                "/api/v1/metadata/manufacturers", "/api/v1/energy/reports/energy")) {
            expect404(get(path));
        }
        expect404(put("/api/v1/users/{id}/suspend", UUID.randomUUID()));
        expect404(get("/api/v1/ruta-que-no-existe"));
    }

    @Test
    void endpointsDeEnergiaDeLaPaginaSiguenActivos() throws Exception {
        for (String path : List.of("/api/v1/energy/stations", "/api/v1/energy/metrics", "/api/v1/energy/alerts",
                "/api/v1/energy/charges")) {
            mockMvc.perform(auth(get(path))).andExpect(status().isOk());
        }
    }

    // ----------------------------------------------------------------------------

    private MockHttpServletRequestBuilder auth(MockHttpServletRequestBuilder builder) {
        return builder.header("Authorization", "Bearer " + tokenA);
    }

    private void expect404(MockHttpServletRequestBuilder builder) throws Exception {
        mockMvc.perform(auth(builder)).andExpect(status().isNotFound());
    }

    @SuppressWarnings("unused")
    private static UserId id(User user) {
        return user.getId();
    }
}
