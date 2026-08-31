package com.casrusil.siierpai.modules.crm.domain.model;

import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class Quote {
    private UUID id;
    private CompanyId companyId;
    private UUID leadId; // Optional, can be direct to an existing client
    private String clientRut;
    private String clientName;
    private LocalDate quoteDate;
    private LocalDate expirationDate;
    private String status; // DRAFT, SENT, ACCEPTED, REJECTED
    private BigDecimal totalAmount;
    private List<QuoteLine> lines;

    public Quote(UUID id, CompanyId companyId, UUID leadId, String clientRut, String clientName,
                 LocalDate quoteDate, LocalDate expirationDate, String status, List<QuoteLine> lines) {
        this.id = id;
        this.companyId = companyId;
        this.leadId = leadId;
        this.clientRut = clientRut;
        this.clientName = clientName;
        this.quoteDate = quoteDate;
        this.expirationDate = expirationDate;
        this.status = status;
        this.lines = lines;
        this.totalAmount = calculateTotal(lines);
    }

    public static Quote create(CompanyId companyId, UUID leadId, String clientRut, String clientName, LocalDate expirationDate, List<QuoteLine> lines) {
        return new Quote(UUID.randomUUID(), companyId, leadId, clientRut, clientName, LocalDate.now(), expirationDate, "DRAFT", lines);
    }

    private BigDecimal calculateTotal(List<QuoteLine> lines) {
        if (lines == null) return BigDecimal.ZERO;
        return lines.stream()
                .map(QuoteLine::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void markAsAccepted() {
        this.status = "ACCEPTED";
    }

    public void markAsRejected() {
        this.status = "REJECTED";
    }

    public UUID getId() { return id; }
    public CompanyId getCompanyId() { return companyId; }
    public UUID getLeadId() { return leadId; }
    public String getClientRut() { return clientRut; }
    public String getClientName() { return clientName; }
    public LocalDate getQuoteDate() { return quoteDate; }
    public LocalDate getExpirationDate() { return expirationDate; }
    public String getStatus() { return status; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public List<QuoteLine> getLines() { return lines; }
}
