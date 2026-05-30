package com.casrusil.siierpai.modules.payroll.domain.port;

import com.casrusil.siierpai.modules.payroll.domain.model.PayrollPeriod;
import java.time.YearMonth;
import java.util.Optional;

public interface PayrollPeriodRepositoryPort {
    PayrollPeriod save(PayrollPeriod period);
    Optional<PayrollPeriod> findByPeriodAndCompanyId(YearMonth period, String companyId);
    Optional<PayrollPeriod> findByIdAndCompanyId(Long id, String companyId);
}
