package com.casrusil.siierpai.modules.payroll.application.service;

import com.casrusil.siierpai.modules.payroll.domain.dto.EmployeeResponse;
import com.casrusil.siierpai.modules.payroll.domain.dto.OpenPayrollPeriodRequest;
import com.casrusil.siierpai.modules.payroll.domain.dto.PayrollPeriodResponse;
import com.casrusil.siierpai.modules.payroll.domain.dto.PayrollProcessRequest;
import com.casrusil.siierpai.modules.payroll.domain.dto.PayslipResponse;

import java.util.List;

public interface PayrollUseCase {

    /**
     * Crea el período de remuneraciones o devuelve el existente.
     *
     * @param request   año, mes y (opcionalmente) UF/UTM
     * @param companyId empresa
     * @return período abierto
     * @throws IllegalStateException si el período ya está cerrado
     */
    PayrollPeriodResponse openPeriod(OpenPayrollPeriodRequest request, String companyId);

    List<PayslipResponse> calculatePayrollForPeriod(PayrollProcessRequest request, String companyId);
    byte[] generatePreviredFile(PayrollProcessRequest request, String companyId);

    /**
     * Lista los empleados de la empresa.
     *
     * @param companyId empresa
     * @return empleados con el contrato que espera el front
     */
    List<EmployeeResponse> getEmployees(String companyId);

    /**
     * Liquidaciones del empleado vinculado al usuario autenticado.
     *
     * @param companyId empresa del usuario
     * @param userId    usuario autenticado
     * @return sólo las liquidaciones de ese empleado
     */
    List<PayslipResponse> getMyPayslips(String companyId, java.util.UUID userId);
}
