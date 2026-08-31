package com.casrusil.siierpai.modules.procurement.domain.model;

import java.math.BigDecimal;
import java.util.UUID;

public class PurchaseOrderLine {
    private UUID id;
    private UUID productId;
    private String description;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalAmount;

    public PurchaseOrderLine(UUID id, UUID productId, String description, BigDecimal quantity, BigDecimal unitPrice) {
        this.id = id;
        this.productId = productId;
        this.description = description;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalAmount = quantity.multiply(unitPrice);
    }

    public UUID getId() { return id; }
    public UUID getProductId() { return productId; }
    public String getDescription() { return description; }
    public BigDecimal getQuantity() { return quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public BigDecimal getTotalAmount() { return totalAmount; }
}
