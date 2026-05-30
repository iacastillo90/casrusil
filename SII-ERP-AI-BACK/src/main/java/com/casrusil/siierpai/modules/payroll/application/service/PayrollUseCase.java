package com.casrusil.siierpai.modules.payroll.application.service;

import com.casrusil.siierpai.modules.payroll.domain.dto.PayrollProcessRequest;
import com.casrusil.siierpai.modules.payroll.domain.dto.PayslipResponse;

import java.util.List;

public interface PayrollUseCase {
    List<PayslipResponse> calculatePayrollForPeriod(PayrollProcessRequest request, String companyId);
    byte[] generatePreviredFile(PayrollProcessRequest request, String companyId);
}
