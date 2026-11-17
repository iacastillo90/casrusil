package com.casrusil.siierpai.modules.integration_sii.domain.model;

import java.util.UUID;

/**
 * Ventana de folios de un CAF, usada por la asignación atómica server-side.
 *
 * @param cafId       identificador del CAF
 * @param initialFolio primer folio autorizado
 * @param finalFolio  último folio autorizado
 * @param currentFolio siguiente folio a asignar dentro de la ventana
 */
public record CafFolioWindow(UUID cafId, long initialFolio, long finalFolio, long currentFolio) {
}
