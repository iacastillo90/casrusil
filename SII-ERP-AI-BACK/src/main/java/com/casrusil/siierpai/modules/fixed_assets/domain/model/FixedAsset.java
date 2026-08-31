package com.casrusil.siierpai.modules.fixed_assets.domain.model;

import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class FixedAsset {
    private final UUID id;
    private final CompanyId companyId;
    private final String name;
    private final String category; // e.g., MACHINERY, VEHICLE
    private final BigDecimal purchaseValue;
    private final LocalDate purchaseDate;
    private BigDecimal currentResidualValue;

    public FixedAsset(UUID id, CompanyId companyId, String name, String category, BigDecimal purchaseValue, LocalDate purchaseDate) {
        this.id = id;
        this.companyId = companyId;
        this.name = name;
        this.category = category;
        this.purchaseValue = purchaseValue;
        this.purchaseDate = purchaseDate;
        this.currentResidualValue = purchaseValue;
    }

    public static FixedAsset create(CompanyId companyId, String name, String category, BigDecimal purchaseValue, LocalDate purchaseDate) {
        return new FixedAsset(UUID.randomUUID(), companyId, name, category, purchaseValue, purchaseDate);
    }

    public void depreciate(BigDecimal amount) {
        this.currentResidualValue = this.currentResidualValue.subtract(amount);
        if (this.currentResidualValue.compareTo(BigDecimal.ONE) < 0) {
            this.currentResidualValue = BigDecimal.ONE; // Valor residual mínimo $1
        }
    }

    public UUID getId() { return id; }
    public CompanyId getCompanyId() { return companyId; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public BigDecimal getPurchaseValue() { return purchaseValue; }
    public LocalDate getPurchaseDate() { return purchaseDate; }
    public BigDecimal getCurrentResidualValue() { return currentResidualValue; }
}