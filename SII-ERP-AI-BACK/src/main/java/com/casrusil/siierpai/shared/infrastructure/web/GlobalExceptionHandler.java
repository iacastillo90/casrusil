package com.casrusil.siierpai.shared.infrastructure.web;

import com.casrusil.siierpai.modules.sso.domain.exception.InvitationNoLongerValidException;
import com.casrusil.siierpai.shared.domain.exception.DomainException;
import com.casrusil.siierpai.shared.domain.exception.ResourceNotFoundException;
import com.casrusil.siierpai.shared.domain.exception.UnprocessableEntityException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * Global exception handler for all REST controllers.
 * Provides consistent error responses and logging.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

        private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

        @ExceptionHandler(ResourceNotFoundException.class)
        public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
                        ResourceNotFoundException ex,
                        HttpServletRequest request) {
                log.warn("Resource not found: {} at {}", ex.getMessage(), request.getRequestURI());

                ErrorResponse error = new ErrorResponse(
                                HttpStatus.NOT_FOUND.value(),
                                "Not Found",
                                ex.getMessage(),
                                request.getRequestURI(),
                                "RESOURCE_NOT_FOUND");

                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        @ExceptionHandler(DomainException.class)
        public ResponseEntity<ErrorResponse> handleDomainException(
                        DomainException ex,
                        HttpServletRequest request) {
                log.warn("Domain exception: {} at {}", ex.getMessage(), request.getRequestURI());

                ErrorResponse error = new ErrorResponse(
                                HttpStatus.BAD_REQUEST.value(),
                                "Bad Request",
                                ex.getMessage(),
                                request.getRequestURI(),
                                "DOMAIN_ERROR");

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        @ExceptionHandler(UnprocessableEntityException.class)
        public ResponseEntity<ErrorResponse> handleUnprocessableEntityException(
                        UnprocessableEntityException ex,
                        HttpServletRequest request) {
                log.warn("Unprocessable entity: {} at {}", ex.getMessage(), request.getRequestURI());

                ErrorResponse error = new ErrorResponse(
                                HttpStatus.UNPROCESSABLE_ENTITY.value(),
                                "Unprocessable Entity",
                                ex.getMessage(),
                                request.getRequestURI(),
                                "UNPROCESSABLE_ENTITY");

                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(error);
        }

        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
                        IllegalArgumentException ex,
                        HttpServletRequest request) {
                log.warn("Illegal argument: {} at {}", ex.getMessage(), request.getRequestURI());

                ErrorResponse error = new ErrorResponse(
                                HttpStatus.BAD_REQUEST.value(),
                                "Bad Request",
                                ex.getMessage(),
                                request.getRequestURI(),
                                "INVALID_ARGUMENT");

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        @ExceptionHandler(IllegalStateException.class)
        public ResponseEntity<ErrorResponse> handleIllegalStateException(
                        IllegalStateException ex,
                        HttpServletRequest request) {
                log.warn("Illegal state: {} at {}", ex.getMessage(), request.getRequestURI());

                ErrorResponse error = new ErrorResponse(
                                HttpStatus.CONFLICT.value(),
                                "Conflict",
                                ex.getMessage(),
                                request.getRequestURI(),
                                "INVALID_STATE");

                return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        }

        @ExceptionHandler(AccessDeniedException.class)
        public ResponseEntity<ErrorResponse> handleAccessDeniedException(
                        AccessDeniedException ex,
                        HttpServletRequest request) {
                log.warn("Access denied: {} at {}", ex.getMessage(), request.getRequestURI());

                ErrorResponse error = new ErrorResponse(
                                HttpStatus.FORBIDDEN.value(),
                                "Forbidden",
                                "You don't have permission to access this resource",
                                request.getRequestURI(),
                                "ACCESS_DENIED");

                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }

        @ExceptionHandler(AuthenticationException.class)
        public ResponseEntity<ErrorResponse> handleAuthenticationException(
                        AuthenticationException ex,
                        HttpServletRequest request) {
                log.warn("Authentication failed: {} at {}", ex.getMessage(), request.getRequestURI());

                ErrorResponse error = new ErrorResponse(
                                HttpStatus.UNAUTHORIZED.value(),
                                "Unauthorized",
                                "Authentication failed",
                                request.getRequestURI(),
                                "AUTHENTICATION_FAILED");

                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ErrorResponse> handleValidationException(
                        MethodArgumentNotValidException ex,
                        HttpServletRequest request) {
                log.warn("Validation failed at {}: {}", request.getRequestURI(), ex.getMessage());

                java.util.Map<String, String> errors = new java.util.HashMap<>();
                ex.getBindingResult().getFieldErrors()
                                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

                ErrorResponse error = new ErrorResponse(
                                HttpStatus.BAD_REQUEST.value(),
                                "Validation Error",
                                "Input validation failed",
                                request.getRequestURI(),
                                errors);

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        @ExceptionHandler(HttpMessageNotReadableException.class)
        public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
                        HttpMessageNotReadableException ex,
                        HttpServletRequest request) {
                log.warn("Malformed request at {}: {}", request.getRequestURI(), ex.getMessage());

                ErrorResponse error = new ErrorResponse(
                                HttpStatus.BAD_REQUEST.value(),
                                "Bad Request",
                                "Malformed JSON request",
                                request.getRequestURI(),
                                "MALFORMED_REQUEST");

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        @ExceptionHandler(org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class)
        public ResponseEntity<ErrorResponse> handleTypeMismatch(
                        org.springframework.web.method.annotation.MethodArgumentTypeMismatchException ex,
                        HttpServletRequest request) {
                log.warn("Type mismatch for '{}' at {}", ex.getName(), request.getRequestURI());
                ErrorResponse error = new ErrorResponse(
                                HttpStatus.BAD_REQUEST.value(),
                                "Bad Request",
                                "Valor inválido para el parámetro '" + ex.getName() + "'",
                                request.getRequestURI(),
                                "INVALID_ARGUMENT");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        @ExceptionHandler(InvitationNoLongerValidException.class)
        public ResponseEntity<ErrorResponse> handleInvitationGone(
                        InvitationNoLongerValidException ex,
                        HttpServletRequest request) {
                log.info("Invitation no longer valid at {}", request.getRequestURI());
                ErrorResponse error = new ErrorResponse(
                                HttpStatus.GONE.value(),
                                "Gone",
                                ex.getMessage(),
                                request.getRequestURI(),
                                "INVITATION_NOT_VALID");
                return ResponseEntity.status(HttpStatus.GONE).body(error);
        }

        /**
         * Excepciones estándar de Spring MVC que ya traen su estado HTTP (ruta inexistente,
         * método no soportado, ResponseStatusException, parámetro faltante, etc.). Antes
         * caían en el handler genérico y respondían 500 (R12).
         */
        @ExceptionHandler({ org.springframework.web.ErrorResponseException.class,
                        jakarta.servlet.ServletException.class,
                        org.springframework.web.multipart.MaxUploadSizeExceededException.class })
        public ResponseEntity<ErrorResponse> handleSpringErrorResponse(
                        Exception ex,
                        HttpServletRequest request) {
                if (!(ex instanceof org.springframework.web.ErrorResponse springError)) {
                        return handleGenericException(ex, request);
                }
                HttpStatus status = HttpStatus.resolve(springError.getStatusCode().value());
                if (status == null) {
                        status = HttpStatus.INTERNAL_SERVER_ERROR;
                }
                if (status.is5xxServerError() && status != HttpStatus.SERVICE_UNAVAILABLE) {
                        return handleGenericException(ex, request);
                }
                log.warn("{} at {}: {}", status.value(), request.getRequestURI(), ex.getMessage());
                String message = switch (status) {
                        case NOT_FOUND -> "Recurso no encontrado";
                        case METHOD_NOT_ALLOWED -> "Método no permitido para esta ruta";
                        case UNSUPPORTED_MEDIA_TYPE -> "Tipo de contenido no soportado";
                        case TOO_MANY_REQUESTS, UNAUTHORIZED, FORBIDDEN, CONFLICT, BAD_REQUEST,
                                        SERVICE_UNAVAILABLE ->
                                springError.getBody().getDetail() != null
                                                ? springError.getBody().getDetail()
                                                : status.getReasonPhrase();
                        default -> status.getReasonPhrase();
                };
                ErrorResponse error = new ErrorResponse(
                                status.value(),
                                status.getReasonPhrase(),
                                message,
                                request.getRequestURI(),
                                status == HttpStatus.NOT_FOUND ? "NOT_FOUND" : "HTTP_" + status.value());
                return ResponseEntity.status(status).body(error);
        }

        /**
         * Error no previsto: el detalle va solo al log, junto a un id de correlación que
         * se entrega al usuario para soporte. Nunca se expone el mensaje interno (R12).
         */
        @ExceptionHandler(Exception.class)
        public ResponseEntity<ErrorResponse> handleGenericException(
                        Exception ex,
                        HttpServletRequest request) {
                String correlationId = CorrelationIds.newId();
                log.error("Unexpected error [correlationId={}] at {}: {}", correlationId,
                                request.getRequestURI(), ex.getMessage(), ex);

                ErrorResponse error = new ErrorResponse(
                                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                "Internal Server Error",
                                "Ocurrió un error inesperado. Si persiste, informa la referencia "
                                                + correlationId + " a soporte.",
                                request.getRequestURI(),
                                "INTERNAL_ERROR");

                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .header("X-Correlation-Id", correlationId)
                                .body(error);
        }
}
