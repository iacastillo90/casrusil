package com.casrusil.siierpai.modules.integration_sii.infrastructure.adapter.in.rest;

import com.casrusil.siierpai.modules.integration_sii.domain.port.in.SiiSyncUseCase;
import com.casrusil.siierpai.modules.integration_sii.infrastructure.adapter.in.rest.dto.SiiSyncResultDto;
import com.casrusil.siierpai.modules.integration_sii.infrastructure.adapter.in.rest.dto.SiiSyncStatusDto;
import com.casrusil.siierpai.shared.infrastructure.context.CompanyContext;
import com.casrusil.siierpai.modules.integration_sii.domain.exception.DteEmissionValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * REST controller exposing the health of the SII integration and on-demand
 * synchronization.
 */
@RestController
@RequestMapping("/api/v1/sii")
@PreAuthorize("hasAnyRole('ADMIN','ACCOUNTANT')")
public class SiiSyncController {

    private final SiiSyncUseCase siiSyncUseCase;

    public SiiSyncController(SiiSyncUseCase siiSyncUseCase) {
        this.siiSyncUseCase = siiSyncUseCase;
    }

    @GetMapping("/status")
    public ResponseEntity<SiiSyncStatusDto> status() {
        return ResponseEntity.ok(SiiSyncStatusDto.from(siiSyncUseCase.getStatus(CompanyContext.requireCompanyId())));
    }

    @ExceptionHandler(DteEmissionValidationException.class)
    public ResponseEntity<Map<String, Object>> handleEmissionValidation(DteEmissionValidationException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", "DATOS_EMISION_INCOMPLETOS");
        body.put("message", ex.getMessage());
        body.put("missingFields", ex.getMissingFields());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(body);
    }

    @PostMapping("/sync")
    public ResponseEntity<SiiSyncResultDto> sync() {
        return ResponseEntity.ok(SiiSyncResultDto.from(siiSyncUseCase.synchronize(CompanyContext.requireCompanyId())));
    }
}
