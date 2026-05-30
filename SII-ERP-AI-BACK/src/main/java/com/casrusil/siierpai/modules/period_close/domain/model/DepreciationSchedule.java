package com.casrusil.siierpai.modules.period_close.domain.model;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.YearMonth;

@Data
@Builder
public class DepreciationSchedule {
    private Long id;
    private String companyId;
    private Long assetId;
    private YearMonth period;
    private BigDecimal depreciationAmount;
    private BigDecimal accumulatedDepreciation;
    private BigDecimal remainingValue;
}
