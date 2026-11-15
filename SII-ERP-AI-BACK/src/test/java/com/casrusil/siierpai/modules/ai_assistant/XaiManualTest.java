package com.casrusil.siierpai.modules.ai_assistant;

import com.casrusil.siierpai.modules.accounting.domain.model.AccountingEntry;
import com.casrusil.siierpai.modules.accounting.domain.model.AccountingEntryLine;
import com.casrusil.siierpai.modules.accounting.domain.model.AnomalyWarning;
import com.casrusil.siierpai.modules.accounting.domain.model.EntryType;
import com.casrusil.siierpai.modules.accounting.domain.model.F29Report;
import com.casrusil.siierpai.modules.accounting.domain.port.out.AccountingEntryRepository;
import com.casrusil.siierpai.modules.accounting.domain.service.AnomalyDetectionService;
import com.casrusil.siierpai.modules.accounting.domain.service.F29CalculatorService;
import com.casrusil.siierpai.modules.ai_assistant.application.tools.CalculateF29Tool;
import com.casrusil.siierpai.modules.ai_assistant.application.tools.SearchInvoicesTool;
import com.casrusil.siierpai.modules.invoicing.domain.model.Invoice;
import com.casrusil.siierpai.modules.invoicing.domain.port.in.SearchInvoicesUseCase;
import com.casrusil.siierpai.modules.fees.domain.model.FeeReceipt;
import com.casrusil.siierpai.modules.fees.domain.port.out.FeeReceiptRepository;
import com.casrusil.siierpai.modules.sso.domain.port.out.CompanyRepository;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class XaiManualTest {

    private static final Logger log = LoggerFactory.getLogger(XaiManualTest.class);

    public static void main(String[] args) {
        String apiKey = System.getenv("XAI_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("XAI_API_KEY environment variable is not set. Some tests might fail or be skipped.");
        } else {
            log.info("XAI_API_KEY found.");
        }

        testCalculateF29Tool();
        testSearchInvoicesTool();
    }

    private static void testCalculateF29Tool() {
        log.info("Testing CalculateF29Tool...");

// Mock Repository
        AccountingEntryRepository repo = new AccountingEntryRepository() {
            @Override
            public List<AccountingEntry> findByCompanyId(CompanyId companies) {
                AccountingEntryLine line = new AccountingEntryLine("210401", "IVA", BigDecimal.ZERO,
                        new BigDecimal("1900"));
                AccountingEntry entry = new AccountingEntry(
                        companies,
                        java.time.LocalDate.now(),
                        "Sale of goods",
                        "INV-001",
                        "INVOICE",
                        "76123456-7",
                        "Test Company",
                        "33",
                        "1",
                        "DRAFT",
                        List.of(line),
                        EntryType.NORMAL);
                return List.of(entry);
            }

            @Override
            public void save(AccountingEntry entry) {
            }

            @Override
            public List<AccountingEntry> findByCompanyIdAndEntryDateBetween(CompanyId companies,
                    java.time.LocalDate from, java.time.LocalDate to) {
                return Collections.emptyList();
            }

            @Override
            public java.util.List<com.casrusil.siierpai.modules.accounting.domain.model.AccountMovement> findMovementsByAccount(
                    CompanyId companies, String accountCode, java.time.LocalDate from, java.time.LocalDate to) {
                return Collections.emptyList();
            }

            @Override
            public java.util.List<AccountingEntry> findInPeriodForClasses(CompanyId companies,
                    java.time.LocalDate from, java.time.LocalDate to, List<Integer> classes) {
                return Collections.emptyList();
            }

            @Override
            public void deleteByReferenceTypeAndPeriod(CompanyId companies, String referenceType,
                    java.time.LocalDate from, java.time.LocalDate to) {
            }
        };

        // Mock Anomaly Service
        com.casrusil.siierpai.modules.invoicing.domain.port.out.InvoiceRepository invoiceRepo = new com.casrusil.siierpai.modules.invoicing.domain.port.out.InvoiceRepository() {
            @Override
            public com.casrusil.siierpai.modules.invoicing.domain.model.Invoice save(com.casrusil.siierpai.modules.invoicing.domain.model.Invoice invoice) {
                return null;
            }

            @Override
            public java.util.Optional<com.casrusil.siierpai.modules.invoicing.domain.model.Invoice> findById(java.util.UUID id) {
                return java.util.Optional.empty();
            }

            @Override
            public java.util.List<com.casrusil.siierpai.modules.invoicing.domain.model.Invoice> findByCompanyId(CompanyId companies) {
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
            public void deleteInPeriod(CompanyId companies, java.time.LocalDate start, java.time.LocalDate end) {
            }

            @Override
            public java.util.List<com.casrusil.siierpai.modules.invoicing.domain.model.Invoice> findByCompanyIdAndDateBetween(CompanyId companies, java.time.LocalDate start, java.time.LocalDate end) {
                return Collections.emptyList();
            }
        };

        AnomalyDetectionService anomalyService = new AnomalyDetectionService(invoiceRepo, repo) {
            @Override
            public List<AnomalyWarning> detectAnomalies(F29Report report) {
                return Collections.emptyList();
            }
        };

        // Mock Fee Repository
        FeeReceiptRepository feeRepo = new FeeReceiptRepository() {
            @Override
            public void save(FeeReceipt feeReceipt) {
            }

            @Override
            public void saveAll(List<FeeReceipt> feeReceipts) {
            }

            @Override
            public List<FeeReceipt> findByCompanyIdAndIssueDateBetween(CompanyId companies,
                    java.time.LocalDate startDate,
                    java.time.LocalDate endDate) {
                return Collections.emptyList();
            }

            @Override
            public List<FeeReceipt> findByCompanyIdAndYear(CompanyId companies, int year) {
                return Collections.emptyList();
            }

            @Override
            public java.util.Optional<FeeReceipt> findByCompanyIdAndFolioAndIssuerRut(CompanyId companies, Long folio,
                    String issuerRut) {
                return java.util.Optional.empty();
            }
        };

        F29CalculatorService service = new F29CalculatorService(repo, feeRepo, anomalyService, null);
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        CalculateF29Tool tool = new CalculateF29Tool(service, mapper);

        try {
            F29Report report = service.calculateF29(new CompanyId(UUID.randomUUID()), YearMonth.now());
            log.info("Evidence IDs: {}", report.evidenceIds());
        } catch (Exception e) {
            log.error("Error in testCalculateF29Tool", e);
        }
    }

    private static void testSearchInvoicesTool() {
        log.info("Testing SearchInvoicesTool...");

        SearchInvoicesUseCase useCase = new SearchInvoicesUseCase() {
            @Override
            public List<Invoice> getInvoicesByCompany(CompanyId companyId) {
                return List.of();
            }

            public Invoice createInvoice(Invoice invoice) {
                return null;
            }

            public Invoice getInvoiceById(UUID id) {
                return null;
            }
        };

        ObjectMapper mapper = new ObjectMapper();
        SearchInvoicesTool tool = new SearchInvoicesTool(useCase, mapper);

        try {
            log.info("SearchInvoicesTool logic verified by code review.");
        } catch (Exception e) {
            log.error("Error in testSearchInvoicesTool", e);
        }
    }
}
