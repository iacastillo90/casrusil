package com.casrusil.siierpai.modules.integration_sii.domain.exception;

import java.util.List;

/**
 * Datos obligatorios del DTE ausentes antes de firmar.
 *
 * <p>
 * Se traduce a HTTP 422: la petición es válida pero el emisor o el receptor no
 * tienen los datos que exige el esquema del SII (giro, acteco, razón social del
 * receptor).
 * </p>
 */
public class DteEmissionValidationException extends RuntimeException {

    private final List<String> missingFields;

    public DteEmissionValidationException(List<String> missingFields) {
        super("Faltan datos obligatorios para emitir el DTE: " + String.join(", ", missingFields));
        this.missingFields = List.copyOf(missingFields);
    }

    public List<String> getMissingFields() {
        return missingFields;
    }
}
