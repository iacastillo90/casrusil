package com.casrusil.siierpai.shared.infrastructure.web;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** R12: sin mensajes internos al usuario y sin 500 para rutas inexistentes. */
class GlobalExceptionHandlerSanitizingTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();
    private final MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/x");

    @Test
    void errorInesperado_noExponeElMensajeInternoYEntregaReferencia() {
        ResponseEntity<ErrorResponse> response = handler.handleGenericException(
                new RuntimeException("PSQLException: relation sso.users password=hunter2"), request);

        assertEquals(500, response.getStatusCode().value());
        String message = response.getBody().message();
        assertFalse(message.contains("PSQL"));
        assertFalse(message.contains("hunter2"));
        String correlationId = response.getHeaders().getFirst("X-Correlation-Id");
        assertNotNull(correlationId);
        assertTrue(message.contains(correlationId));
    }

    @Test
    void rutaInexistente_responde404() {
        ResponseEntity<ErrorResponse> response = handler.handleSpringErrorResponse(
                new NoResourceFoundException(HttpMethod.GET, "api/v1/no-existe"), request);

        assertEquals(404, response.getStatusCode().value());
        assertEquals("NOT_FOUND", response.getBody().errorCode());
    }

    @Test
    void responseStatusException_conservaSuEstadoYMensajeClaro() {
        ResponseEntity<ErrorResponse> response = handler.handleSpringErrorResponse(
                new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Servicio de sesiones no disponible"),
                request);

        assertEquals(503, response.getStatusCode().value());
        assertEquals("Servicio de sesiones no disponible", response.getBody().message());
    }
}
