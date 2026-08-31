package com.casrusil.siierpai.modules.crm.domain.model;

import java.math.BigDecimal;
import java.util.UUID;

public class QuoteLine {
    private UUID id;
    private String description;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalAmount;

    public QuoteLine(UUID id, String description, BigDecimal quantity, BigDecimal unitPrice) {
        this.id = id;
        this.description = description;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalAmount = quantity.multiply(unitPrice);
    }

    public UUID getId() { return id; }
    public String getDescription() { return description; }
    public BigDecimal getQuantity() { return quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public BigDecimal getTotalAmount() { return totalAmount; }
}
