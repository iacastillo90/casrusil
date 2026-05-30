package com.casrusil.siierpai.modules.period_close.application.service;

import com.casrusil.siierpai.modules.period_close.domain.dto.PeriodAuditResponse;
import com.casrusil.siierpai.modules.period_close.domain.dto.PeriodCloseRequest;

public interface PeriodCloseUseCase {
    PeriodAuditResponse auditPeriod(PeriodCloseRequest request, String companyId);
    void executeClose(PeriodCloseRequest request, String companyId);
}
