package com.casrusil.siierpai.modules.accounting.infrastructure.adapter.in.rest;

import jakarta.validation.Valid;

import com.casrusil.siierpai.modules.accounting.domain.model.Account;
import com.casrusil.siierpai.modules.accounting.domain.model.AccountType;
import com.casrusil.siierpai.modules.accounting.domain.model.AccountingEntry;
import com.casrusil.siierpai.modules.accounting.domain.model.ClosedPeriod;
import com.casrusil.siierpai.modules.accounting.domain.model.F29Report;
import com.casrusil.siierpai.modules.accounting.domain.port.out.AccountRepository;
import com.casrusil.siierpai.modules.accounting.domain.port.out.AccountingEntryRepository;
import com.casrusil.siierpai.modules.accounting.domain.service.F29CalculatorService;
import com.casrusil.siierpai.modules.accounting.domain.service.PeriodClosingService;
import com.casrusil.siierpai.modules.accounting.domain.service.BalanceSheetService;
import com.casrusil.siierpai.modules.accounting.domain.model.BalanceSheetReport;
import com.casrusil.siierpai.modules.accounting.infrastructure.adapter.in.rest.dto.AccountNodeDto;
import com.casrusil.siierpai.modules.accounting.infrastructure.adapter.in.rest.dto.AccountingEntryApiDto;
import com.casrusil.siierpai.modules.invoicing.domain.model.Invoice;
import com.casrusil.siierpai.modules.invoicing.domain.port.in.ManageInvoiceUseCase;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import com.casrusil.siierpai.shared.domain.valueobject.UserId;
import com.casrusil.siierpai.shared.infrastructure.context.CompanyContext;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;

/**
 * Controlador REST para la gestión contable.
 * 
 * <p>
 * Expone endpoints para consultar asientos, gestionar el plan de cuentas,
 * calcular impuestos mensuales (F29) y realizar el cierre de periodos.
 * 
 * <h2>Endpoints principales:</h2>
 * <ul>
 * <li>{@code GET /api/v1/accounting/entries}: Listar asientos contables.</li>
 * <li>{@code POST /api/v1/accounting/accounts}: Crear cuenta contable.</li>
 * <li>{@code GET /api/v1/accounting/f29}: Calcular borrador F29.</li>
 * <li>{@code POST /api/v1/accounting/periods/close}: Cerrar periodo
 * contable.</li>
 * </ul>
 * 
 * @since 1.0
 */
@RestController
@RequestMapping("/api/v1/accounting")
@PreAuthorize("hasAnyRole('ADMIN','ACCOUNTANT')")
public class AccountingController {

    private static final LocalDate BALANCE_START_DATE = LocalDate.of(1900, 1, 1);

    private final AccountingEntryRepository accountingEntryRepository;
    private final AccountRepository accountRepository;
    private final F29CalculatorService f29CalculatorService;
    private final PeriodClosingService periodClosingService;
    private final com.casrusil.siierpai.modules.accounting.application.service.OpeningBalanceService openingBalanceService;
    private final com.casrusil.siierpai.modules.accounting.domain.service.AccountingEntryService accountingEntryService;
    private final com.casrusil.siierpai.modules.accounting.infrastructure.parser.BalanceSheetParser balanceSheetParser;
    private final BalanceSheetService balanceSheetService;
    private final com.casrusil.siierpai.modules.accounting.application.service.ReportingService reportingService;
    private final ManageInvoiceUseCase manageInvoiceUseCase;

    public AccountingController(AccountingEntryRepository accountingEntryRepository,
            AccountRepository accountRepository,
            F29CalculatorService f29CalculatorService,
            PeriodClosingService periodClosingService,
            com.casrusil.siierpai.modules.accounting.application.service.OpeningBalanceService openingBalanceService,
            com.casrusil.siierpai.modules.accounting.domain.service.AccountingEntryService accountingEntryService,
            com.casrusil.siierpai.modules.accounting.infrastructure.parser.BalanceSheetParser balanceSheetParser,
            BalanceSheetService balanceSheetService,
            com.casrusil.siierpai.modules.accounting.application.service.ReportingService reportingService,
            ManageInvoiceUseCase manageInvoiceUseCase) {
        this.accountingEntryRepository = accountingEntryRepository;
        this.accountRepository = accountRepository;
        this.f29CalculatorService = f29CalculatorService;
        this.periodClosingService = periodClosingService;
        this.openingBalanceService = openingBalanceService;
        this.accountingEntryService = accountingEntryService;
        this.balanceSheetParser = balanceSheetParser;
        this.balanceSheetService = balanceSheetService;
        this.reportingService = reportingService;
        this.manageInvoiceUseCase = manageInvoiceUseCase;
    }

    @GetMapping("/reports/income-statement")
    public ResponseEntity<com.casrusil.siierpai.modules.accounting.domain.dto.IncomeStatementReportDTO> getIncomeStatement(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {
        // Lógica de fallback para mes/año actual
        int targetMonth = (month != null) ? month : LocalDate.now().getMonthValue();
        int targetYear = (year != null) ? year : LocalDate.now().getYear();

        LocalDate start = LocalDate.of(targetYear, targetMonth, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

        return ResponseEntity.ok(reportingService.generateIncomeStatement(start, end));
    }

    @PostMapping("/opening-balance/upload")
    public ResponseEntity<java.util.Map<String, Object>> uploadOpeningBalance(
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        CompanyId companyId = CompanyContext.requireCompanyId();
        try {
            List<com.casrusil.siierpai.modules.accounting.application.service.OpeningBalanceService.OpeningBalanceItem> items = balanceSheetParser
                    .parseTsv(file.getInputStream());
            openingBalanceService.setOpeningBalance(companyId, items);
            return ResponseEntity
                    .ok(java.util.Map.of("message", "Opening balance imported successfully", "count", items.size()));
        } catch (java.io.IOException e) {
            return ResponseEntity.internalServerError()
                    .body(java.util.Map.of("error", "Failed to parse file: " + e.getMessage()));
        }
    }

    @PostMapping("/opening-balance")
    public ResponseEntity<Void> setOpeningBalance(
            @Valid @RequestBody List<com.casrusil.siierpai.modules.accounting.application.service.OpeningBalanceService.OpeningBalanceItem> items) {
        CompanyId companyId = CompanyContext.requireCompanyId();
        openingBalanceService.setOpeningBalance(companyId, items);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/entries")
    public ResponseEntity<List<AccountingEntry>> getEntries() {
        CompanyId companyId = CompanyContext.requireCompanyId();
        return ResponseEntity.ok(accountingEntryRepository.findByCompanyId(companyId));
    }

    @GetMapping("/entries/by-invoice/{invoiceId}")
    @PreAuthorize("hasAnyRole('ADMIN','SALES','ACCOUNTANT')")
    public ResponseEntity<List<AccountingEntryApiDto>> getEntriesByInvoice(@PathVariable java.util.UUID invoiceId) {
        CompanyId companyId = CompanyContext.requireCompanyId();
        Invoice invoice = manageInvoiceUseCase.getInvoice(invoiceId);
        List<AccountingEntryApiDto> entries = accountingEntryRepository
                .findByCompanyIdAndReferenceTypeInAndReferenceId(companyId, List.of("INVOICE", "FACTURA_AI"),
                        String.valueOf(invoice.getFolio()))
                .stream()
                .map(AccountingEntryApiDto::from)
                .toList();
        return ResponseEntity.ok(entries);
    }

    @GetMapping("/accounts/tree")
    public ResponseEntity<List<AccountNodeDto>> getAccountTree(
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate date) {
        CompanyId companyId = CompanyContext.requireCompanyId();
        LocalDate asOf = date != null ? date : LocalDate.now();

        List<Account> accounts = accountRepository.findAll(companyId);
        Map<String, BigDecimal> balances = new HashMap<>();
        accounts.forEach(account -> balances.put(account.getCode(), BigDecimal.ZERO));
        accountingEntryRepository
                .findByCompanyIdAndEntryDateBetween(companyId, BALANCE_START_DATE, asOf)
                .forEach(entry -> entry.getLines().forEach(line -> {
                    if (balances.containsKey(line.accountCode())) {
                        BigDecimal debit = line.debit() != null ? line.debit() : BigDecimal.ZERO;
                        BigDecimal credit = line.credit() != null ? line.credit() : BigDecimal.ZERO;
                        balances.merge(line.accountCode(), debit.subtract(credit), BigDecimal::add);
                    }
                }));

        Map<String, MutableAccountNode> nodes = new java.util.LinkedHashMap<>();
        accounts.stream()
                .sorted(Comparator.comparing(Account::getCode))
                .forEach(account -> nodes.put(account.getCode(), new MutableAccountNode(account)));

        List<MutableAccountNode> roots = new ArrayList<>();
        for (MutableAccountNode node : nodes.values()) {
            MutableAccountNode parent = findParent(node, nodes);
            if (parent != null) {
                parent.children.add(node);
            } else {
                roots.add(node);
            }
        }

        return ResponseEntity.ok(roots.stream()
                .map(node -> toAccountNodeDto(node, balances))
                .toList());
    }

    @GetMapping("/ledger/{accountCode}")
    public ResponseEntity<List<com.casrusil.siierpai.modules.accounting.domain.model.AccountMovement>> getLedger(
            @PathVariable String accountCode,
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate from,
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate to) {
        CompanyId companyId = CompanyContext.requireCompanyId();
        List<com.casrusil.siierpai.modules.accounting.domain.model.AccountMovement> ledger = accountingEntryService
                .getLedger(companyId, accountCode, from, to);
        return ResponseEntity.ok(ledger);
    }

    @PostMapping("/accounts")
    public ResponseEntity<Account> createAccount(@Valid @RequestBody CreateAccountRequest request) {
        CompanyId companyId = CompanyContext.requireCompanyId();
        Account account = new Account(
                companyId,
                request.code(),
                request.name(),
                request.type(),
                request.description());
        Account savedAccount = accountRepository.save(account);
        return ResponseEntity.ok(savedAccount);
    }

    @GetMapping("/accounts")
    public ResponseEntity<List<Account>> getAccounts() {
        CompanyId companyId = CompanyContext.requireCompanyId();
        return ResponseEntity.ok(accountRepository.findAll(companyId));
    }

    @GetMapping("/balance-sheet")
    public ResponseEntity<BalanceSheetReport> getBalanceSheet(@RequestParam String date) {
        CompanyId companyId = CompanyContext.requireCompanyId();
        LocalDate reportDate = LocalDate.parse(date);
        BalanceSheetReport report = balanceSheetService.generateBalanceSheet(companyId, reportDate);
        return ResponseEntity.ok(report);
    }

    // ========== F29 Endpoints ==========

    @GetMapping("/f29")
    public ResponseEntity<F29Report> calculateF29(@RequestParam String period) {
        CompanyId companyId = CompanyContext.requireCompanyId();
        YearMonth yearMonth = parsePeriod(period);
        F29Report report = f29CalculatorService.calculateF29(companyId, yearMonth);
        return ResponseEntity.ok(report);
    }

    // ========== Period Closing Endpoints ==========

    @PostMapping("/periods/close")
    public ResponseEntity<ClosedPeriod> closePeriod(
            @RequestParam String period,
            @RequestParam String userId) {
        CompanyId companyId = CompanyContext.requireCompanyId();
        YearMonth yearMonth = parsePeriod(period);
        UserId user = new UserId(java.util.UUID.fromString(userId));
        ClosedPeriod closedPeriod = periodClosingService.closePeriod(companyId, yearMonth, user);
        return ResponseEntity.ok(closedPeriod);
    }

    @GetMapping("/periods/closed")
    public ResponseEntity<List<ClosedPeriod>> getClosedPeriods() {
        CompanyId companyId = CompanyContext.requireCompanyId();
        List<ClosedPeriod> closedPeriods = periodClosingService.getClosedPeriods(companyId);
        return ResponseEntity.ok(closedPeriods);
    }

    @GetMapping("/periods/{period}/status")
    public ResponseEntity<PeriodStatusResponse> getPeriodStatus(@PathVariable String period) {
        CompanyId companyId = CompanyContext.requireCompanyId();
        YearMonth yearMonth = parsePeriod(period);
        boolean isClosed = periodClosingService.isPeriodClosed(companyId, yearMonth);
        return ResponseEntity.ok(new PeriodStatusResponse(period, isClosed));
    }

    // ========== Helper Methods ==========

    private MutableAccountNode findParent(MutableAccountNode node, Map<String, MutableAccountNode> nodes) {
        String code = node.account.getCode();
        MutableAccountNode parent = null;
        int bestLength = -1;
        for (MutableAccountNode candidate : nodes.values()) {
            String candidateCode = candidate.account.getCode();
            if (candidateCode.length() < code.length()
                    && code.startsWith(candidateCode)
                    && candidateCode.length() > bestLength) {
                parent = candidate;
                bestLength = candidateCode.length();
            }
        }
        return parent;
    }

    private AccountNodeDto toAccountNodeDto(MutableAccountNode node, Map<String, BigDecimal> balances) {
        String type = switch (node.account.getType()) {
            case GAIN -> "REVENUE";
            case LOSS -> "EXPENSE";
            default -> node.account.getType().name();
        };
        List<AccountNodeDto> children = node.children.stream()
                .map(child -> toAccountNodeDto(child, balances))
                .toList();
        return new AccountNodeDto(
                node.account.getCode(),
                node.account.getName(),
                type,
                children,
                balances.getOrDefault(node.account.getCode(), BigDecimal.ZERO),
                children.isEmpty());
    }

    private static final class MutableAccountNode {
        private final Account account;
        private final List<MutableAccountNode> children = new ArrayList<>();

        private MutableAccountNode(Account account) {
            this.account = account;
        }
    }

    private YearMonth parsePeriod(String period) {
        if (period.contains("-")) {
            return YearMonth.parse(period);
        } else {
            return YearMonth.parse(period, java.time.format.DateTimeFormatter.ofPattern("yyyyMM"));
        }
    }

    // ========== Request/Response Records ==========

    public record CreateAccountRequest(
            String code,
            String name,
            AccountType type,
            String description) {
    }

    public record PeriodStatusResponse(
            String period,
            boolean closed) {
    }
}
