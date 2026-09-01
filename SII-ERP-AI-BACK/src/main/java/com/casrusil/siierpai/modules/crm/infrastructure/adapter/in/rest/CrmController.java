package com.casrusil.siierpai.modules.crm.infrastructure.adapter.in.rest;

import jakarta.validation.Valid;

import com.casrusil.siierpai.modules.crm.domain.model.Lead;
import com.casrusil.siierpai.modules.crm.domain.model.LeadStatus;
import com.casrusil.siierpai.modules.crm.domain.model.Quote;
import com.casrusil.siierpai.modules.crm.domain.model.QuoteLine;
import com.casrusil.siierpai.modules.crm.domain.port.in.CrmUseCase;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import com.casrusil.siierpai.shared.infrastructure.context.CompanyContext;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/crm")
@PreAuthorize("hasAnyRole('ADMIN','SALES')")
public class CrmController {

    private final CrmUseCase crmUseCase;

    public CrmController(CrmUseCase crmUseCase) {
        this.crmUseCase = crmUseCase;
    }

    // --- LEADS ---

    @GetMapping("/leads")
    public ResponseEntity<List<Lead>> getLeads() {
        CompanyId companyId = CompanyContext.getCompanyId();
        return ResponseEntity.ok(crmUseCase.getLeadsByCompany(companyId));
    }

    @PostMapping("/leads")
    public ResponseEntity<Lead> createLead(@Valid @RequestBody CreateLeadRequest request) {
        CompanyId companyId = CompanyContext.getCompanyId();
        Lead lead = crmUseCase.createLead(companyId, request.contactName(), request.companyName(), request.email(), request.phone());
        return ResponseEntity.ok(lead);
    }

    @PutMapping("/leads/{id}/status")
    public ResponseEntity<Lead> updateLeadStatus(@PathVariable UUID id, @Valid @RequestBody UpdateLeadStatusRequest request) {
        Lead lead = crmUseCase.updateLeadStatus(id, LeadStatus.valueOf(request.status()));
        return ResponseEntity.ok(lead);
    }

    // --- QUOTES ---

    @GetMapping("/quotes")
    public ResponseEntity<List<Quote>> getQuotes() {
        CompanyId companyId = CompanyContext.getCompanyId();
        return ResponseEntity.ok(crmUseCase.getQuotesByCompany(companyId));
    }

    @PostMapping("/quotes")
    public ResponseEntity<Quote> createQuote(@Valid @RequestBody CreateQuoteRequest request) {
        CompanyId companyId = CompanyContext.getCompanyId();
        
        List<QuoteLine> lines = request.lines().stream()
                .map(l -> new QuoteLine(UUID.randomUUID(), l.description(), l.quantity(), l.unitPrice()))
                .collect(Collectors.toList());

        Quote quote = crmUseCase.createQuote(
                companyId,
                request.leadId(),
                request.clientRut(),
                request.clientName(),
                request.expirationDate(),
                lines
        );

        return ResponseEntity.ok(quote);
    }

    @PostMapping("/quotes/{id}/accept")
    public ResponseEntity<Quote> acceptQuote(@PathVariable UUID id) {
        return ResponseEntity.ok(crmUseCase.acceptQuote(id));
    }

    @PostMapping("/quotes/{id}/reject")
    public ResponseEntity<Quote> rejectQuote(@PathVariable UUID id) {
        return ResponseEntity.ok(crmUseCase.rejectQuote(id));
    }

    public record CreateLeadRequest(String contactName, String companyName, String email, String phone) {}
    public record UpdateLeadStatusRequest(String status) {}

    public record CreateQuoteRequest(
            UUID leadId,
            String clientRut,
            String clientName,
            LocalDate expirationDate,
            List<CreateQuoteLineRequest> lines
    ) {}

    public record CreateQuoteLineRequest(
            String description,
            BigDecimal quantity,
            BigDecimal unitPrice
    ) {}
}
