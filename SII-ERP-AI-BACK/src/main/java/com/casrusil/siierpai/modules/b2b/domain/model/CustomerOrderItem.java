package com.casrusil.siierpai.modules.b2b.domain.model;

import java.math.BigDecimal;

public record CustomerOrderItem(String productName, int quantity, BigDecimal unitPrice) {

    public CustomerOrderItem {
        if (productName == null || productName.isBlank()) {
            throw new IllegalArgumentException("productName is required");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be greater than zero");
        }
        if (unitPrice == null || unitPrice.signum() < 0) {
            throw new IllegalArgumentException("unitPrice must be zero or positive");
        }
    }

    public BigDecimal subtotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
