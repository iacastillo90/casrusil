package com.casrusil.siierpai.modules.dashboard.infrastructure.adapter.in.rest;

import com.casrusil.siierpai.modules.accounting.domain.model.AccountingEntry;
import com.casrusil.siierpai.modules.accounting.domain.port.out.AccountingEntryRepository;
import com.casrusil.siierpai.modules.dashboard.infrastructure.adapter.in.rest.dto.RecentActivityItemDto;
import com.casrusil.siierpai.modules.invoicing.domain.model.Invoice;
import com.casrusil.siierpai.modules.invoicing.domain.port.out.InvoiceRepository;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import com.casrusil.siierpai.shared.infrastructure.context.CompanyContext;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/v1/dashboard")
@PreAuthorize("hasAnyRole('ADMIN','OPERATIONS','SALES','ACCOUNTANT')")
public class RecentActivityController {

    private static final ZoneId BUSINESS_ZONE = ZoneId.of("America/Santiago");
    private static final int MAX_LIMIT = 50;

    private final InvoiceRepository invoiceRepository;
    private final AccountingEntryRepository accountingEntryRepository;

    public RecentActivityController(InvoiceRepository invoiceRepository,
            AccountingEntryRepository accountingEntryRepository) {
        this.invoiceRepository = invoiceRepository;
        this.accountingEntryRepository = accountingEntryRepository;
    }

    @GetMapping("/recent-activity")
    public ResponseEntity<List<RecentActivityItemDto>> getRecentActivity(
            @RequestParam(defaultValue = "10") int limit) {
        int safeLimit = Math.max(1, Math.min(limit, MAX_LIMIT));
        CompanyId companyId = CompanyContext.requireCompanyId();

        List<RecentActivityItemDto> activities = new ArrayList<>();
        invoiceRepository.findRecentByCompanyId(companyId, safeLimit)
                .forEach(invoice -> activities.add(fromInvoice(invoice)));
        accountingEntryRepository.findRecentByCompanyId(companyId, safeLimit)
                .forEach(entry -> activities.add(fromEntry(entry)));

        List<RecentActivityItemDto> result = activities.stream()
                .sorted(Comparator.comparing(RecentActivityItemDto::timestamp).reversed())
                .limit(safeLimit)
                .toList();
        return ResponseEntity.ok(result);
    }

    private RecentActivityItemDto fromInvoice(Invoice invoice) {
        return new RecentActivityItemDto(
                invoice.getId().toString(),
                "Factura folio " + invoice.getFolio() + " emitida",
                invoice.getTotalAmount(),
                "Sistema",
                invoice.getDate().atStartOfDay(BUSINESS_ZONE).toInstant());
    }

    private RecentActivityItemDto fromEntry(AccountingEntry entry) {
        BigDecimal amount = entry.getLines().stream()
                .map(line -> line.debit() != null ? line.debit() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new RecentActivityItemDto(
                "entry-" + entry.getId(),
                entry.getDescription(),
                amount,
                "Sistema",
                entry.getEntryDate().atStartOfDay(BUSINESS_ZONE).toInstant());
    }
}
