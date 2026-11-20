package com.casrusil.siierpai.modules.accounting.infrastructure.web;

import com.casrusil.siierpai.modules.accounting.application.service.DataCleanupService;
import org.springframework.http.ResponseEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint de limpieza puntual de datos (RCV de noviembre).
 *
 * <p>
 * Se mantiene sólo en el perfil {@code dev}: borra asientos contables de forma
 * irreversible y sin pasar por las reglas del dominio. En producción no debe
 * existir; si hace falta una corrección de datos, se ejecuta como tarea
 * manual auditada.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/accounting/cleanup")
@PreAuthorize("hasRole('ADMIN')")
@Profile("dev")
public class DataCleanupController {

    private final DataCleanupService cleanupService;

    public DataCleanupController(DataCleanupService cleanupService) {
        this.cleanupService = cleanupService;
    }

    @PostMapping("/november-rcv")
    public ResponseEntity<String> cleanupNovemberRcv() {
        cleanupService.cleanupNovemberRcv();
        return ResponseEntity.ok("Successfully cleaned up November RCV entries.");
    }
}
