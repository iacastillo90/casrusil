package com.casrusil.siierpai.modules.treasury.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Movimiento de caja registrado en tesorería.
 */
@Entity
@Table(name = "treasury_cash_transactions")
public class CashTransactionEntity {

    @Id
    private UUID id;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "reference")
    private String reference;

    @Column(name = "category")
    private String category;

    @Column(name = "currency", nullable = false)
    private String currency;

    @Column(name = "exchange_rate", nullable = false)
    private BigDecimal exchangeRate;

    public CashTransactionEntity() {
    }

    public CashTransactionEntity(UUID id, UUID companyId, LocalDate date, String description, BigDecimal amount,
            String reference, String category, String currency, BigDecimal exchangeRate) {
        this.id = id;
        this.companyId = companyId;
        this.date = date;
        this.description = description;
        this.amount = amount;
        this.reference = reference;
        this.category = category;
        this.currency = currency;
        this.exchangeRate = exchangeRate;
    }

    public UUID getId() {
        return id;
    }

    public UUID getCompanyId() {
        return companyId;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getReference() {
        return reference;
    }

    public String getCategory() {
        return category;
    }

    public String getCurrency() {
        return currency;
    }

    public BigDecimal getExchangeRate() {
        return exchangeRate;
    }
}
