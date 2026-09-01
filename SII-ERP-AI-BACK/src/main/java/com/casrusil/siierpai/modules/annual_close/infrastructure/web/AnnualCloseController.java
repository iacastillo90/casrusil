package com.casrusil.siierpai.modules.annual_close.infrastructure.web;

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
    private static final String COMPANY_HEADER = "X-Company-Id";

    @PostMapping("/f22/calculate/{fiscalYear}")
    public ResponseEntity<AnnualCloseResponse> calculateF22(
            @PathVariable int fiscalYear,
            @RequestHeader(COMPANY_HEADER) String companyId) {
        
        return ResponseEntity.ok(annualCloseUseCase.calculateAnnualTaxes(fiscalYear, companyId));
    }
}
