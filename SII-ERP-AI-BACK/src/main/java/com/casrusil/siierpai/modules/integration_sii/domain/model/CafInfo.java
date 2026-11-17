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

    /**
     * Calcula el estado de un CAF a partir de sus folios disponibles y su fecha
     * de vencimiento.
     *
     * @param disponibles       folios disponibles (puede ser nulo)
     * @param fechaVencimiento  fecha de vencimiento del CAF (puede ser nula)
     * @param today             fecha de referencia
     * @param umbralPorAgotarse cantidad de folios desde la cual se considera
     *                          por agotarse
     * @return estado calculado del CAF
     */
    public static EstadoCaf evaluateEstado(Long disponibles, LocalDate fechaVencimiento, LocalDate today,
            long umbralPorAgotarse) {
        if (fechaVencimiento != null && fechaVencimiento.isBefore(today)) {
            return EstadoCaf.VENCIDO;
        }
        if (disponibles == null || disponibles <= 0) {
            return EstadoCaf.AGOTADO;
        }
        if (disponibles <= umbralPorAgotarse) {
            return EstadoCaf.POR_AGOTARSE;
        }
        return EstadoCaf.VIGENTE;
    }
}
