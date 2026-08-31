package com.casrusil.siierpai.modules.crm.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "crm_quotes")
public class QuoteEntity {

    @Id
    private UUID id;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "lead_id")
    private UUID leadId;

    @Column(name = "client_rut", nullable = false)
    private String clientRut;

    @Column(name = "client_name", nullable = false)
    private String clientName;

    @Column(name = "quote_date", nullable = false)
    private LocalDate quoteDate;

    @Column(name = "expiration_date")
    private LocalDate expirationDate;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "quote_id")
    private List<QuoteLineEntity> lines;

    protected QuoteEntity() {}

    public QuoteEntity(UUID id, UUID companyId, UUID leadId, String clientRut, String clientName, LocalDate quoteDate, LocalDate expirationDate, String status, BigDecimal totalAmount, List<QuoteLineEntity> lines) {
        this.id = id;
        this.companyId = companyId;
        this.leadId = leadId;
        this.clientRut = clientRut;
        this.clientName = clientName;
        this.quoteDate = quoteDate;
        this.expirationDate = expirationDate;
        this.status = status;
        this.totalAmount = totalAmount;
        this.lines = lines;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getCompanyId() { return companyId; }
    public void setCompanyId(UUID companyId) { this.companyId = companyId; }
    public UUID getLeadId() { return leadId; }
    public void setLeadId(UUID leadId) { this.leadId = leadId; }
    public String getClientRut() { return clientRut; }
    public void setClientRut(String clientRut) { this.clientRut = clientRut; }
    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }
    public LocalDate getQuoteDate() { return quoteDate; }
    public void setQuoteDate(LocalDate quoteDate) { this.quoteDate = quoteDate; }
    public LocalDate getExpirationDate() { return expirationDate; }
    public void setExpirationDate(LocalDate expirationDate) { this.expirationDate = expirationDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public List<QuoteLineEntity> getLines() { return lines; }
    public void setLines(List<QuoteLineEntity> lines) { this.lines = lines; }
}
