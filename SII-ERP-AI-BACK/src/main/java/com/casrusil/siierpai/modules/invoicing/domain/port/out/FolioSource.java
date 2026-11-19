package com.casrusil.siierpai.modules.invoicing.domain.port.out;

import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;

import java.util.Optional;

/**
 * Puerto de salida para la asignación server-side de folios desde los rangos
 * autorizados por el SII (CAF).
 *
 * <p>
 * La implementación debe reservar el folio de forma atómica dentro de la
 * transacción del caso de uso que crea el documento, de modo que dos
 * peticiones concurrentes nunca obtengan el mismo folio.
 * </p>
 *
 * @since 1.0
 */
public interface FolioSource {

    /**
     * Reserva el siguiente folio disponible para el tipo de DTE indicado.
     *
     * @param companyId empresa dueña de los CAF
     * @param tipoDte   código numérico del tipo de DTE (ej: 33)
     * @return el folio reservado, o vacío si no hay CAF con folios disponibles
     */
    Optional<Long> reserveNextFolio(CompanyId companyId, int tipoDte);
}
