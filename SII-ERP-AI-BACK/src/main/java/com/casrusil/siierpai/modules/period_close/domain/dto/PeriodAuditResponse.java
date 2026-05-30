package com.casrusil.siierpai.modules.period_close.domain.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class PeriodAuditResponse {
    private String period;
    private List<String> issuesFound;
    private boolean readyToClose;
    private String aiRecommendation;
}
