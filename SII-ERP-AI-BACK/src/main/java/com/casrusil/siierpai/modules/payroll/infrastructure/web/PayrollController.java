package com.casrusil.siierpai.modules.payroll.infrastructure.web;

import jakarta.validation.Valid;

import com.casrusil.siierpai.modules.payroll.application.service.PayrollUseCase;
import com.casrusil.siierpai.modules.payroll.domain.dto.PayrollProcessRequest;
import com.casrusil.siierpai.modules.payroll.domain.dto.PayslipResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payroll")
@RequiredArgsConstructor
public class PayrollController {

    private final PayrollUseCase payrollUseCase;
    
    private static final String COMPANY_HEADER = "X-Company-Id";

    @PostMapping("/calculate")
    public ResponseEntity<List<PayslipResponse>> calculatePayroll(
            @RequestHeader(COMPANY_HEADER) String companyId,
            @Valid @RequestBody PayrollProcessRequest request) {
        return ResponseEntity.ok(payrollUseCase.calculatePayrollForPeriod(request, companyId));
    }

    @PostMapping("/previred/export")
    public ResponseEntity<byte[]> exportPreviredFile(
            @RequestHeader(COMPANY_HEADER) String companyId,
            @Valid @RequestBody PayrollProcessRequest request) {
            
        byte[] fileContent = payrollUseCase.generatePreviredFile(request, companyId);
        
        String filename = String.format("previred_%s_%s.txt", request.getYear(), request.getMonth());
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.TEXT_PLAIN)
                .body(fileContent);
    }

    @GetMapping("/employee/payslips")
    public ResponseEntity<List<PayslipResponse>> getMyPayslips(
            @RequestHeader(value = "X-Employee-Id", defaultValue = "00000000-0000-0000-0000-000000000000") String employeeId) {
        // In a real scenario, employeeId is extracted from JWT (SecurityContextHolder).
        return ResponseEntity.ok(payrollUseCase.getEmployeePayslips(employeeId));
    }
}
