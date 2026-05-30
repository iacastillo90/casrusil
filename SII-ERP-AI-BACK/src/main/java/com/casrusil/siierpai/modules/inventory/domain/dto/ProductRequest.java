package com.casrusil.siierpai.modules.inventory.domain.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class ProductRequest {
    private String sku;
    private String name;
    private String description;
    private BigDecimal salePrice;
    private String category;
}
