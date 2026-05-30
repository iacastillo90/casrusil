package com.casrusil.siierpai.modules.inventory.domain.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class ProductResponse {
    private Long id;
    private String sku;
    private String name;
    private String description;
    private BigDecimal currentStock;
    private BigDecimal averageCost;
    private BigDecimal salePrice;
    private String category;
}
