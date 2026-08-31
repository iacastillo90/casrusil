package com.casrusil.siierpai.modules.treasury.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record CashFlowForecast(
    LocalDate targetDate,
    BigDecimal startingBalance,
    BigDecimal expectedInflows,
    BigDecimal expectedOutflows,
    BigDecimal projectedBalance,
    List<RiskFactor> riskFactors
) {
    public record RiskFactor(String riskLevel, String description) {}
}