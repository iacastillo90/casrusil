package com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.ocpp.session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gestor de sesiones WebSocket OCPP.
 * Mantiene el mapa de sesiones activas y gestiona heartbeats.
 * 
 * Thread-safe usando ConcurrentHashMap.
 */
@Component
public class OcppSessionManager {

    private static final Logger logger = LoggerFactory.getLogger(OcppSessionManager.class);

    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, LocalDateTime> lastHeartbeat = new ConcurrentHashMap<>();

    /**
     * Registra una nueva sesión WebSocket.
     */
    public void registerSession(String ocppId, WebSocketSession session) {
        sessions.put(ocppId, session);
        lastHeartbeat.put(ocppId, LocalDateTime.now());
        logger.info("Sesión registrada: {}", ocppId);
    }

    /**
     * Elimina una sesión WebSocket.
     */
    public void removeSession(String ocppId) {
        sessions.remove(ocppId);
        lastHeartbeat.remove(ocppId);
        logger.info("Sesión eliminada: {}", ocppId);
    }

    /**
     * Actualiza el timestamp del último heartbeat.
     */
    public void updateHeartbeat(String ocppId) {
        lastHeartbeat.put(ocppId, LocalDateTime.now());
    }

    /**
     * Obtiene sesiones que no han enviado heartbeat en el tiempo especificado.
     */
    public List<String> getStaleSessions(java.time.Duration timeout) {
        LocalDateTime threshold = LocalDateTime.now().minus(timeout);
        return lastHeartbeat.entrySet().stream()
                .filter(e -> e.getValue().isBefore(threshold))
                .map(Map.Entry::getKey)
                .toList();
    }

    /**
     * Envía un mensaje a un cargador específico.
     */
    public void sendMessage(String ocppId, String message) throws IOException {
        WebSocketSession session = sessions.get(ocppId);
        if (session != null && session.isOpen()) {
            synchronized (session) {
                session.sendMessage(new TextMessage(message));
            }
            logger.debug("Mensaje enviado a {}: {}", ocppId, message);
        } else {
            logger.warn("Sesión no disponible para {}", ocppId);
            throw new IOException("Sesión WebSocket no disponible");
        }
    }

    /**
     * Verifica si un cargador está conectado.
     */
    public boolean isConnected(String ocppId) {
        WebSocketSession session = sessions.get(ocppId);
        return session != null && session.isOpen();
    }

    /**
     * Obtiene el número de sesiones activas.
     */
    public int getActiveSessionCount() {
        return sessions.size();
    }

    /**
     * Obtiene todas las sesiones activas.
     */
    public List<String> getActiveOcppIds() {
        return List.copyOf(sessions.keySet());
    }
}
