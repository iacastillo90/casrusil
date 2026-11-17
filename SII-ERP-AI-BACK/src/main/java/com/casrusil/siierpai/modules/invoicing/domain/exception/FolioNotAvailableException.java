package com.casrusil.siierpai.modules.invoicing.domain.exception;

/**
 * Excepción lanzada cuando no existe un CAF vigente con folios disponibles
 * para el tipo de DTE solicitado.
 *
 * <p>
 * Hereda de {@link IllegalStateException}, que el
 * {@code GlobalExceptionHandler} mapea a HTTP 409 (Conflict).
 * </p>
 *
 * @since 1.0
 */
public class FolioNotAvailableException extends IllegalStateException {

    public FolioNotAvailableException(int tipoDte) {
        super("No hay folios disponibles para el tipo DTE " + tipoDte
                + ". Cargue un CAF vigente con folios disponibles.");
    }
}
