package com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.ocpp.config;

import com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.ocpp.handler.OcppWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * Configuración de WebSocket para OCPP.
 * 
 * Endpoint: ws://localhost:8080/ocpp/{ocppId}
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final OcppWebSocketHandler ocppHandler;

    public WebSocketConfig(OcppWebSocketHandler ocppHandler) {
        this.ocppHandler = ocppHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(ocppHandler, "/ocpp/{ocppId}")
                .setAllowedOrigins("*"); // En producción, restringir a dominios específicos
    }
}
