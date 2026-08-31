package com.casrusil.siierpai.modules.period_close.infrastructure.web;

import jakarta.validation.Valid;

import com.casrusil.siierpai.modules.period_close.application.service.PeriodCloseUseCase;
import com.casrusil.siierpai.modules.period_close.domain.dto.PeriodAuditResponse;
import com.casrusil.siierpai.modules.period_close.domain.dto.PeriodCloseRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/period-close")
@RequiredArgsConstructor
public class PeriodCloseController {

    private final PeriodCloseUseCase periodCloseUseCase;
    private static final String COMPANY_HEADER = "X-Company-Id";

    @PostMapping("/audit")
    public ResponseEntity<PeriodAuditResponse> auditPeriod(
            @RequestHeader(COMPANY_HEADER) String companyId,
            @Valid @RequestBody PeriodCloseRequest request) {
        return ResponseEntity.ok(periodCloseUseCase.auditPeriod(request, companyId));
    }

    @PostMapping("/execute")
    public ResponseEntity<Void> executeClose(
            @RequestHeader(COMPANY_HEADER) String companyId,
            @Valid @RequestBody PeriodCloseRequest request) {
        periodCloseUseCase.executeClose(request, companyId);
        return ResponseEntity.noContent().build();
    }
}
