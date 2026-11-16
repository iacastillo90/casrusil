package com.casrusil.siierpai.modules.integration_sii.infrastructure.adapter.in.rest;

import com.casrusil.siierpai.modules.integration_sii.domain.port.in.SiiDteUseCase;
import com.casrusil.siierpai.modules.integration_sii.infrastructure.adapter.in.rest.dto.DteSendResultDto;
import com.casrusil.siierpai.modules.integration_sii.infrastructure.adapter.in.rest.dto.DteStatusDto;
import com.casrusil.siierpai.shared.infrastructure.context.CompanyContext;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for DTE status lookups and DTE submission to the SII.
 */
@RestController
@RequestMapping("/api/v1/sii/dte")
@PreAuthorize("hasAnyRole('ADMIN','ACCOUNTANT')")
public class SiiDteController {

    private final SiiDteUseCase siiDteUseCase;

    public SiiDteController(SiiDteUseCase siiDteUseCase) {
        this.siiDteUseCase = siiDteUseCase;
    }

    @GetMapping("/{tipo}/{folio}")
    public ResponseEntity<DteStatusDto> status(@PathVariable String tipo, @PathVariable String folio) {
        return ResponseEntity.ok(DteStatusDto.from(
                siiDteUseCase.getDte(CompanyContext.requireCompanyId(), tipo, folio)));
    }

    @PostMapping("/{dteId}/send")
    public ResponseEntity<DteSendResultDto> send(@PathVariable String dteId) {
        return ResponseEntity.ok(DteSendResultDto.from(
                siiDteUseCase.sendDte(CompanyContext.requireCompanyId(), dteId)));
    }
}
