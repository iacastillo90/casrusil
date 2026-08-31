package com.casrusil.siierpai.modules.treasury.domain.model;

import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class CashTransaction {
    private final UUID id;
    private final CompanyId companyId;
    private final LocalDate date;
    private final String description;
    private final BigDecimal amount; // Positive for IN, Negative for OUT
    private final String reference;
    private final String category; // e.g., "OFFICE_SUPPLIES", "SALES_CASH"
    private final String currency; // "CLP", "USD", "EUR"
    private final BigDecimal exchangeRate; // Rate against base currency (CLP)

    public CashTransaction(UUID id, CompanyId companyId, LocalDate date, String description, BigDecimal amount,
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

    public static CashTransaction create(CompanyId companyId, LocalDate date, String description, BigDecimal amount,
            String reference, String category, String currency, BigDecimal exchangeRate) {
        return new CashTransaction(UUID.randomUUID(), companyId, date, description, amount, reference, category, currency, exchangeRate);
    }

    public UUID getId() {
        return id;
    }

    public CompanyId getCompanyId() {
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
