package com.casrusil.siierpai.modules.period_close.infrastructure.web;

import com.casrusil.siierpai.shared.infrastructure.context.CompanyContext;

import jakarta.validation.Valid;

import com.casrusil.siierpai.modules.period_close.application.service.PeriodCloseUseCase;
import com.casrusil.siierpai.modules.period_close.domain.dto.PeriodAuditResponse;
import com.casrusil.siierpai.modules.period_close.domain.dto.PeriodCloseRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/period-close")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','ACCOUNTANT')")
public class PeriodCloseController {

    private final PeriodCloseUseCase periodCloseUseCase;

    /** Empresa del usuario autenticado (JWT). Nunca se acepta desde la petición. */
    private static String currentCompany() {
        return CompanyContext.requireCompanyId().value().toString();
    }

    @PostMapping("/audit")
    public ResponseEntity<PeriodAuditResponse> auditPeriod(
            @Valid @RequestBody PeriodCloseRequest request) {
        return ResponseEntity.ok(periodCloseUseCase.auditPeriod(request, currentCompany()));
    }

    @PostMapping("/execute")
    public ResponseEntity<Void> executeClose(
            @Valid @RequestBody PeriodCloseRequest request) {
        periodCloseUseCase.executeClose(request, currentCompany());
        return ResponseEntity.noContent().build();
    }
}
