package com.casrusil.siierpai.shared.domain.exception;

public class BusinessValidationException extends DomainException {
    public BusinessValidationException(String message) {
        super(message);
    }

    public BusinessValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
