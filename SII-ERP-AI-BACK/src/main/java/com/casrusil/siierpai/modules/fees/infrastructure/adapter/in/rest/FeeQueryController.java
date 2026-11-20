package com.casrusil.siierpai.modules.fees.infrastructure.adapter.in.rest;

import com.casrusil.siierpai.modules.fees.application.service.FeeReceiptService;
import com.casrusil.siierpai.modules.fees.domain.dto.FeeReceiptResponse;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import com.casrusil.siierpai.shared.infrastructure.context.CompanyContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Consulta de boletas de honorarios (BHE) recibidas.
 *
 * <p>
 * SEGURIDAD: la empresa se resuelve desde {@link CompanyContext} (JWT). Nunca se
 * acepta {@code companyId} por query param para evitar acceso cruzado entre
 * tenants.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/fees")
@Tag(name = "Fees", description = "Boletas de honorarios electrónicas recibidas")
@PreAuthorize("hasAnyRole('ADMIN','ACCOUNTANT')")
public class FeeQueryController {

    private final FeeReceiptService feeReceiptService;

    public FeeQueryController(FeeReceiptService feeReceiptService) {
        this.feeReceiptService = feeReceiptService;
    }

    /**
     * Lista las boletas de honorarios de la empresa actual.
     *
     * @param year año de emisión opcional; si se omite devuelve todos los registros
     * @return boletas ordenadas por fecha de emisión descendente y folio descendente
     */
    @GetMapping
    @Operation(summary = "Listar boletas de honorarios de la empresa")
    public ResponseEntity<List<FeeReceiptResponse>> listFees(
            @RequestParam(name = "year", required = false) Integer year) {

        CompanyId companyId = CompanyContext.requireCompanyId();
        List<FeeReceiptResponse> response = feeReceiptService.listFees(companyId, year)
                .stream()
                .map(FeeReceiptResponse::from)
                .toList();

        return ResponseEntity.ok(response);
    }
}
