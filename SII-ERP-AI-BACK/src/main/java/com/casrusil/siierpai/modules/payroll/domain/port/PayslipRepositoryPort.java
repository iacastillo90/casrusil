package com.casrusil.siierpai.modules.payroll.domain.port;

import com.casrusil.siierpai.modules.payroll.domain.model.Payslip;
import java.util.List;

public interface PayslipRepositoryPort {
    Payslip save(Payslip payslip);
    List<Payslip> findByPeriodIdAndCompanyId(Long periodId, String companyId);
    List<Payslip> findByEmployeeId(String employeeId);
}
