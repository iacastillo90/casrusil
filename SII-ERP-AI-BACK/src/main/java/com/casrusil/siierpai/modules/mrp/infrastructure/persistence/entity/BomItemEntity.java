package com.casrusil.siierpai.modules.mrp.infrastructure.persistence.entity;

import jakarta.persistence.Embeddable;
import lombok.Data;
import java.math.BigDecimal;

@Embeddable
@Data
public class BomItemEntity {
    private String componentProductId;
    private BigDecimal quantity;
}