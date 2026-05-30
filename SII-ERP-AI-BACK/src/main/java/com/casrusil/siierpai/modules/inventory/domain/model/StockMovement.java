package com.casrusil.siierpai.modules.inventory.domain.model;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class StockMovement {
    private Long id;
    private String companyId;
    private Long productId;
    private MovementType type;
    private BigDecimal quantity;
    private BigDecimal unitCost;
    private BigDecimal totalCost;
    private LocalDateTime movementDate;
    private String referenceDocument;
    private String description;
}
