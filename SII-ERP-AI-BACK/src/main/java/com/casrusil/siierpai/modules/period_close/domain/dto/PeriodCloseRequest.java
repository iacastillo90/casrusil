package com.casrusil.siierpai.modules.period_close.domain.dto;

import lombok.Builder;
import lombok.Data;
import java.time.YearMonth;
import java.util.List;

@Data
@Builder
public class PeriodCloseRequest {
    private int year;
    private int month;
}
