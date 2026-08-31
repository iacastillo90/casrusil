package com.casrusil.b2b.infrastructure.persistence.entity;

import jakarta.persistence.Embeddable;
import lombok.Data;
import java.math.BigDecimal;

@Embeddable
@Data
public class B2bOrderItemEntity {
    private String productId;
    private int quantity;
    private BigDecimal unitPrice;
}
