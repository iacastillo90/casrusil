package com.casrusil.siierpai.modules.payroll.infrastructure.web;

import jakarta.validation.Valid;

import com.casrusil.siierpai.modules.payroll.application.service.PayrollUseCase;
import com.casrusil.siierpai.modules.payroll.domain.dto.OpenPayrollPeriodRequest;
import com.casrusil.siierpai.modules.payroll.domain.dto.PayrollPeriodResponse;
import com.casrusil.siierpai.modules.payroll.domain.dto.PayrollProcessRequest;
import com.casrusil.siierpai.modules.payroll.domain.dto.PayslipResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payroll")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','HR')")
public class PayrollController {

    private final PayrollUseCase payrollUseCase;

    /** Empresa del usuario autenticado (JWT). Nunca se acepta desde la petición. */
    private static String currentCompany() {
        return com.casrusil.siierpai.shared.infrastructure.context.CompanyContext.requireCompanyId().value().toString();
    }

    /**
     * Crea el período de remuneraciones o devuelve el existente (idempotente).
     * Un período cerrado responde 409.
     */
    @PostMapping("/periods")
    public ResponseEntity<PayrollPeriodResponse> openPeriod(
            @Valid @RequestBody OpenPayrollPeriodRequest request) {
        return ResponseEntity.ok(payrollUseCase.openPeriod(request, currentCompany()));
    }

    @PostMapping("/calculate")
    public ResponseEntity<List<PayslipResponse>> calculatePayroll(
            @Valid @RequestBody PayrollProcessRequest request) {
        return ResponseEntity.ok(payrollUseCase.calculatePayrollForPeriod(request, currentCompany()));
    }

    @PostMapping("/previred/export")
    public ResponseEntity<byte[]> exportPreviredFile(
            @Valid @RequestBody PayrollProcessRequest request) {
            
        byte[] fileContent = payrollUseCase.generatePreviredFile(request, currentCompany());
        
        String filename = String.format("previred_%s_%s.txt", request.getYear(), request.getMonth());
        
        // El archivo va en ANSI/Windows-1252 (requisito de carga de Previred) y
        // CRLF; el Content-Type debe declarar ese charset, no el UTF-8 por defecto.
        MediaType contentType = MediaType.parseMediaType("text/plain;charset=windows-1252");

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(contentType)
                .body(fileContent);
    }

    /**
     * Lista los empleados de la empresa actual.
     *
     * @return empleados con id, nombre, RUT, sueldo base, AFP e Isapre
     */
    @GetMapping("/employees")
    public ResponseEntity<List<com.casrusil.siierpai.modules.payroll.domain.dto.EmployeeResponse>> getEmployees() {
        var companyId = com.casrusil.siierpai.shared.infrastructure.context.CompanyContext.requireCompanyId();
        return ResponseEntity.ok(payrollUseCase.getEmployees(companyId.value().toString()));
    }

    /**
     * Obtiene las liquidaciones del empleado vinculado al usuario autenticado.
     *
     * <p>
     * El empleado se resuelve desde el {@link UserId} del JWT y la empresa del
     * tenant; se ignora cualquier header de empleado para evitar que un USER
     * consulte liquidaciones ajenas.
     * </p>
     */
    @GetMapping("/employee/payslips")
    @PreAuthorize("hasAnyRole('ADMIN','HR','USER')")
    public ResponseEntity<List<PayslipResponse>> getMyPayslips() {
        var authentication = org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication();
        if (authentication == null
                || !(authentication.getPrincipal() instanceof com.casrusil.siierpai.shared.domain.valueobject.UserId userId)) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).build();
        }
        var companyId = com.casrusil.siierpai.shared.infrastructure.context.CompanyContext.requireCompanyId();
        return ResponseEntity.ok(payrollUseCase.getMyPayslips(companyId.value().toString(), userId.value()));
    }
}
