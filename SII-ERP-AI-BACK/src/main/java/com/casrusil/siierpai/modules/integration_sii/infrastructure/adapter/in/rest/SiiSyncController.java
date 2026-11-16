package com.casrusil.siierpai.modules.integration_sii.infrastructure.adapter.in.rest;

import com.casrusil.siierpai.modules.integration_sii.domain.port.in.SiiSyncUseCase;
import com.casrusil.siierpai.modules.integration_sii.infrastructure.adapter.in.rest.dto.SiiSyncResultDto;
import com.casrusil.siierpai.modules.integration_sii.infrastructure.adapter.in.rest.dto.SiiSyncStatusDto;
import com.casrusil.siierpai.shared.infrastructure.context.CompanyContext;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @PostMapping("/sync")
    public ResponseEntity<SiiSyncResultDto> sync() {
        return ResponseEntity.ok(SiiSyncResultDto.from(siiSyncUseCase.synchronize(CompanyContext.requireCompanyId())));
    }
}
