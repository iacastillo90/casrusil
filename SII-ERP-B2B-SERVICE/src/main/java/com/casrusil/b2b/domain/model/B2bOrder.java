package com.casrusil.b2b.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record B2bOrder(
    UUID id,
    String companyId,
    String customerId,
    List<B2bOrderItem> items,
    BigDecimal totalAmount,
    String status,
    LocalDate createdAt
) {
    public record B2bOrderItem(String productId, int quantity, BigDecimal unitPrice) {}
}
