package com.casrusil.siierpai.modules.integration_sii.infrastructure.adapter.in.rest;

import com.casrusil.siierpai.modules.integration_sii.domain.model.Caf;
import com.casrusil.siierpai.modules.integration_sii.application.service.CafQueryService;
import com.casrusil.siierpai.modules.integration_sii.domain.port.out.CafRepository;
import com.casrusil.siierpai.modules.integration_sii.infrastructure.adapter.in.rest.dto.CafStatusResponse;
import com.casrusil.siierpai.modules.integration_sii.infrastructure.adapter.in.rest.dto.CafUploadResponse;
import com.casrusil.siierpai.modules.integration_sii.infrastructure.parser.CafParser;
import com.casrusil.siierpai.shared.infrastructure.context.CompanyContext;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Controlador REST para la gestión de CAF (Código de Autorización de Folios).
 *
 * <p>
 * Permite la carga de archivos XML de folios autorizados por el SII.
 * Estos archivos son necesarios para emitir facturas electrónicas válidas.
 *
 * <h2>Endpoints:</h2>
 * <ul>
 * <li>{@code POST /api/v1/sii/caf}: Cargar archivo CAF XML.</li>
 * <li>{@code GET /api/v1/sii/caf}: Listar CAF con su estado y folios disponibles.</li>
 * </ul>
 *
 * @see CafParser
 * @since 1.0
 */
@RestController
@RequestMapping("/api/v1/sii/caf")
@PreAuthorize("hasAnyRole('ADMIN','ACCOUNTANT')")
public class CafController {

    private final CafParser cafParser;
    private final CafRepository cafRepository;
    private final CafQueryService cafQueryService;

    public CafController(CafParser cafParser, CafRepository cafRepository, CafQueryService cafQueryService) {
        this.cafParser = cafParser;
        this.cafRepository = cafRepository;
        this.cafQueryService = cafQueryService;
    }

    @GetMapping
    public ResponseEntity<List<CafStatusResponse>> listCafs() {
        return ResponseEntity.ok(cafQueryService.listCafs().stream()
                .map(CafStatusResponse::from)
                .toList());
    }

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<?> uploadCaf(@RequestParam("file") MultipartFile file) {
        try {
            String xmlContent = new String(file.getBytes(), StandardCharsets.ISO_8859_1); // SII uses ISO-8859-1
            Caf caf = cafParser.parse(xmlContent);

            cafRepository.save(CompanyContext.requireCompanyId(), caf);

            return ResponseEntity.ok(CafUploadResponse.from(caf));

        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Error reading file: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error processing CAF: " + e.getMessage());
        }
    }
}
