package com.casrusil.siierpai.shared.domain.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record StockMovedEvent(
        String companyId,
        Long productId,
        String movementType, // IN, OUT
        BigDecimal quantity,
        BigDecimal unitPrice,
        BigDecimal totalAmount,
        LocalDateTime movementDate
) {}
