package com.casrusil.siierpai.modules.period_close.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.YearMonth;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PeriodCloseRequest {
    private int year;
    private int month;
}
