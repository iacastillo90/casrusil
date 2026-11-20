package com.casrusil.siierpai.shared.domain.exception;

/**
 * Error de negocio que impide procesar una solicitud válida por falta de
 * configuración o datos requeridos (HTTP 422), distinto de un fallo interno.
 */
public class UnprocessableEntityException extends DomainException {
    public UnprocessableEntityException(String message) {
        super(message);
    }

    public UnprocessableEntityException(String message, Throwable cause) {
        super(message, cause);
    }
}
