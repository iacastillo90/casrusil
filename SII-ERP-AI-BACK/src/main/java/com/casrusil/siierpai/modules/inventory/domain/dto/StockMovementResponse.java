package com.casrusil.siierpai.modules.inventory.domain.dto;

import com.casrusil.siierpai.modules.inventory.domain.model.MovementType;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class StockMovementResponse {
    private Long id;
    private Long productId;
    private MovementType type;
    private BigDecimal quantity;
    private BigDecimal unitCost;
    private BigDecimal totalCost;
    private LocalDateTime movementDate;
    private String referenceDocument;
    private String description;
}
