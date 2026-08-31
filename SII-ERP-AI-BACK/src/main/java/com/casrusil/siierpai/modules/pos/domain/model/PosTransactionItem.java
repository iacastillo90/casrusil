package com.casrusil.siierpai.modules.pos.domain.model;

import java.math.BigDecimal;
import java.util.UUID;

public class PosTransactionItem {
    private UUID id;
    private String productId;
    private String productName;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;

    public PosTransactionItem(UUID id, String productId, String productName, BigDecimal quantity, BigDecimal unitPrice) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.subtotal = quantity.multiply(unitPrice);
    }

    public UUID getId() { return id; }
    public String getProductId() { return productId; }
    public String getProductName() { return productName; }
    public BigDecimal getQuantity() { return quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public BigDecimal getSubtotal() { return subtotal; }
}
