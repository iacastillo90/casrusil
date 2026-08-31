package com.casrusil.siierpai.modules.wms.infrastructure.persistence.entity;

import jakarta.persistence.Embeddable;
import lombok.Data;

@Embeddable
@Data
public class PickingItemEntity {
    private String productId;
    private int quantity;
    private String locationCode;
}