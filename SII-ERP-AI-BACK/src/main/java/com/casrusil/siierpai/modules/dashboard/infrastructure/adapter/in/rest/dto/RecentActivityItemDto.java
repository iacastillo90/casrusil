package com.casrusil.siierpai.modules.dashboard.infrastructure.adapter.in.rest.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record RecentActivityItemDto(
        String id,
        String description,
        BigDecimal amount,
        String user,
        Instant timestamp) {
}
