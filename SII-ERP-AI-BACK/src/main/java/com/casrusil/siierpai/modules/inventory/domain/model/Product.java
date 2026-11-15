package com.casrusil.siierpai.modules.inventory.domain.model;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class Product {
    private Long id;
    private String companyId;
    private String sku;
    private String name;
    private String description;
    private BigDecimal currentStock;
    private BigDecimal minStock;
    private BigDecimal averageCost;
    private BigDecimal salePrice;
    private String category;
}
