package com.casrusil.siierpai.modules.integration_sii.domain.model;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Información completa de un CAF con su estado actual y folios disponibles.
 */
public record CafInfo(
        UUID id,
        String tipoDte,
        Long folioDesde,
        Long folioHasta,
        Long folioActual,
        Long disponibles,
        LocalDate fechaAutorizacion,
        LocalDate fechaVencimiento,
        EstadoCaf estado
) {
    public enum EstadoCaf {
        VIGENTE,
        POR_AGOTARSE,
        AGOTADO,
        VENCIDO
    }
}
