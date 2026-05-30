package com.casrusil.siierpai.modules.period_close.domain.model;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

@Data
@Builder
public class PeriodAuditLog {
    private Long id;
    private String companyId;
    private YearMonth period;
    private LocalDateTime auditDate;
    private List<String> issuesFound;
    private boolean isResolved;
}
