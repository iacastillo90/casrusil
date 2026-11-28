package com.casrusil.siierpai.modules.annual_close.infrastructure.web;

import com.casrusil.siierpai.shared.infrastructure.context.CompanyContext;

import com.casrusil.siierpai.modules.annual_close.application.service.AnnualCloseUseCase;
import com.casrusil.siierpai.modules.annual_close.domain.dto.AnnualCloseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/annual-close")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','ACCOUNTANT')")
public class AnnualCloseController {

    private final AnnualCloseUseCase annualCloseUseCase;

    @PostMapping("/f22/calculate/{fiscalYear}")
    public ResponseEntity<AnnualCloseResponse> calculateF22(
            @PathVariable int fiscalYear) {
        // Empresa del usuario autenticado (JWT). Nunca se acepta desde la petición.
        String companyId = CompanyContext.requireCompanyId().value().toString();
        return ResponseEntity.ok(annualCloseUseCase.calculateAnnualTaxes(fiscalYear, companyId));
    }
}
