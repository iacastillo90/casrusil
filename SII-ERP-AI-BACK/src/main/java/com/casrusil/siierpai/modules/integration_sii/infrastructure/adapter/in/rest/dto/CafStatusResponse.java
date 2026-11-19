package com.casrusil.siierpai.modules.integration_sii.infrastructure.adapter.in.rest.dto;

import com.casrusil.siierpai.modules.integration_sii.domain.model.CafInfo;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Estado de un CAF expuesto al frontend.
 *
 * @param id                identificador del CAF
 * @param tipoDte           código del tipo de DTE
 * @param folioDesde        primer folio autorizado
 * @param folioHasta        último folio autorizado
 * @param folioActual       siguiente folio a asignar
 * @param disponibles       folios disponibles
 * @param fechaAutorizacion fecha de autorización del CAF
 * @param fechaVencimiento  fecha de vencimiento del CAF
 * @param estado            estado calculado (VIGENTE, POR_AGOTARSE, AGOTADO, VENCIDO)
 */
public record CafStatusResponse(
        UUID id,
        Integer tipoDte,
        Long folioDesde,
        Long folioHasta,
        Long folioActual,
        Long disponibles,
        LocalDate fechaAutorizacion,
        LocalDate fechaVencimiento,
        String estado) {

    public static CafStatusResponse from(CafInfo caf) {
        Integer tipoDte = null;
        if (caf.tipoDte() != null) {
            try {
                tipoDte = Integer.parseInt(caf.tipoDte());
            } catch (NumberFormatException ignored) {
                tipoDte = null;
            }
        }

        return new CafStatusResponse(
                caf.id(),
                tipoDte,
                caf.folioDesde(),
                caf.folioHasta(),
                caf.folioActual(),
                caf.disponibles(),
                caf.fechaAutorizacion(),
                caf.fechaVencimiento(),
                caf.estado() != null ? caf.estado().name() : "DESCONOCIDO");
    }
}
