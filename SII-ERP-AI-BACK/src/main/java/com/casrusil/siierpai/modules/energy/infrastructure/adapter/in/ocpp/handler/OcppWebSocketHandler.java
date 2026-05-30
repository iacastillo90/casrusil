package com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.ocpp.handler;

import com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.ocpp.model.OcppMessage;
import com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.ocpp.parser.OcppMessageParser;
import com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.ocpp.session.OcppSessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.net.URI;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Handler WebSocket para OCPP usando Java 25 Virtual Threads.
 * 
 * Cada conexión de cargador se procesa en su propio Virtual Thread ligero.
 * Esto permite manejar miles de conexiones concurrentes con mínimo overhead.
 * 
 * Endpoint: ws://localhost:8080/ocpp/{ocppId}
 */
@Component
public class OcppWebSocketHandler extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(OcppWebSocketHandler.class);

    private final OcppSessionManager sessionManager;
    private final OcppMessageParser messageParser;
    private final OcppMessageDispatcher messageDispatcher;

    // Executor con Virtual Threads (Java 25)
    private final ExecutorService virtualThreadExecutor;

    public OcppWebSocketHandler(
            OcppSessionManager sessionManager,
            OcppMessageParser messageParser,
            OcppMessageDispatcher messageDispatcher) {
        this.sessionManager = sessionManager;
        this.messageParser = messageParser;
        this.messageDispatcher = messageDispatcher;

        // Crear executor con Virtual Threads
        this.virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        // Procesar nueva conexión en Virtual Thread
        virtualThreadExecutor.submit(() -> {
            try {
                String ocppId = extractOcppId(session);
                sessionManager.registerSession(ocppId, session);
                logger.info("Cargador conectado: {} (Virtual Thread: {})",
                        ocppId, Thread.currentThread());
            } catch (Exception e) {
                logger.error("Error al establecer conexión", e);
                closeSession(session);
            }
        });
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        // Procesar cada mensaje en su propio Virtual Thread
        virtualThreadExecutor.submit(() -> {
            String ocppId = null;
            try {
                ocppId = extractOcppId(session);
                String payload = message.getPayload();

                logger.debug("Mensaje recibido de {}: {}", ocppId, payload);

                // 1. Parsear mensaje OCPP
                OcppMessage ocppMessage = messageParser.parseMessage(payload);

                // 2. Manejar según tipo de mensaje
                if (ocppMessage.isCall()) {
                    // CALL: Request del cargador al servidor
                    handleCall(ocppMessage, ocppId, session);
                } else if (ocppMessage.isCallResult()) {
                    // CALLRESULT: Respuesta del cargador a comando del servidor
                    handleCallResult(ocppMessage);
                } else if (ocppMessage.isCallError()) {
                    // CALLERROR: Error del cargador
                    handleCallError(ocppMessage);
                }

            } catch (Exception e) {
                logger.error("Error al procesar mensaje de {}", ocppId, e);
                sendErrorResponse(session, ocppId, e);
            }
        });
    }

    private void handleCall(OcppMessage ocppMessage, String ocppId, WebSocketSession session) throws Exception {
        // 3. Despachar a handler correspondiente
        Object response = messageDispatcher.dispatch(
                ocppMessage.action(),
                ocppMessage.payload(),
                ocppId);

        // 4. Serializar y enviar respuesta
        String responseJson = messageParser.serializeCallResult(
                ocppMessage.uniqueId(),
                response);

        sessionManager.sendMessage(ocppId, responseJson);
    }

    private void handleCallResult(OcppMessage ocppMessage) {
        // Completar CompletableFuture del comando pendiente
        messageDispatcher.completeCommand(ocppMessage.uniqueId(), ocppMessage.payload());
    }

    private void handleCallError(OcppMessage ocppMessage) {
        // Fallar CompletableFuture del comando pendiente
        messageDispatcher.failCommand(
                ocppMessage.uniqueId(),
                ocppMessage.errorCode(),
                ocppMessage.errorDescription());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        virtualThreadExecutor.submit(() -> {
            try {
                String ocppId = extractOcppId(session);
                sessionManager.removeSession(ocppId);
                logger.info("Cargador desconectado: {} (Status: {})", ocppId, status);
            } catch (Exception e) {
                logger.error("Error al cerrar conexión", e);
            }
        });
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        virtualThreadExecutor.submit(() -> {
            try {
                String ocppId = extractOcppId(session);
                logger.error("Error de transporte para {}", ocppId, exception);
                sessionManager.removeSession(ocppId);
            } catch (Exception e) {
                logger.error("Error al manejar error de transporte", e);
            }
        });
    }

    /**
     * Extrae el OCPP ID del path de la sesión WebSocket.
     * Formato: /ocpp/{ocppId}
     */
    private String extractOcppId(WebSocketSession session) {
        URI uri = session.getUri();
        if (uri == null) {
            throw new IllegalStateException("URI de sesión es null");
        }

        String path = uri.getPath();
        String[] parts = path.split("/");

        if (parts.length < 3) {
            throw new IllegalArgumentException("Path inválido: " + path);
        }

        return parts[2]; // /ocpp/{ocppId}
    }

    /**
     * Envía una respuesta de error al cargador.
     */
    private void sendErrorResponse(WebSocketSession session, String ocppId, Exception e) {
        try {
            String errorJson = messageParser.serializeCallError(
                    "unknown",
                    "InternalError",
                    e.getMessage());
            sessionManager.sendMessage(ocppId, errorJson);
        } catch (Exception ex) {
            logger.error("Error al enviar respuesta de error", ex);
        }
    }

    /**
     * Cierra una sesión WebSocket de forma segura.
     */
    private void closeSession(WebSocketSession session) {
        try {
            if (session.isOpen()) {
                session.close(CloseStatus.SERVER_ERROR);
            }
        } catch (Exception e) {
            logger.error("Error al cerrar sesión", e);
        }
    }
}
