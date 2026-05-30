package com.casrusil.siierpai.shared.domain.event;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Map;

public record PayrollCalculatedEvent(
        String companyId,
        YearMonth period,
        Map<String, BigDecimal> totalDiscounts,
        BigDecimal totalNetSalary,
        BigDecimal totalTaxableIncome
) {}
