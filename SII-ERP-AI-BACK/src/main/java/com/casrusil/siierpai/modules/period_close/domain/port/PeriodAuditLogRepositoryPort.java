package com.casrusil.siierpai.modules.period_close.domain.port;

import com.casrusil.siierpai.modules.period_close.domain.model.PeriodAuditLog;
import java.time.YearMonth;
import java.util.Optional;

public interface PeriodAuditLogRepositoryPort {
    PeriodAuditLog save(PeriodAuditLog log);
    Optional<PeriodAuditLog> findByPeriodAndCompanyId(YearMonth period, String companyId);
}
