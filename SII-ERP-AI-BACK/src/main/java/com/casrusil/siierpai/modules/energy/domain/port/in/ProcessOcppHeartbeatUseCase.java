package com.casrusil.siierpai.modules.energy.domain.port.in;

import com.casrusil.siierpai.modules.energy.domain.model.*;

/**
 * Puerto de Entrada: Caso de Uso - Procesar Heartbeat OCPP.
 * Permite procesar mensajes de heartbeat de los cargadores (OCPP Heartbeat).
 * 
 * Patrón: Hexagonal Architecture - Input Port (Use Case)
 * Implementación: En application/service
 */
public interface ProcessOcppHeartbeatUseCase {

    /**
     * Procesa un heartbeat de un cargador.
     * Actualiza el estado del cargador a ONLINE si estaba OFFLINE.
     * 
     * @param command Comando con el OCPP ID del cargador
     */
    void execute(HeartbeatCommand command);

    /**
     * Comando para procesar heartbeat.
     */
    record HeartbeatCommand(
            OcppIdentifier chargerOcppId) {
        public HeartbeatCommand {
            if (chargerOcppId == null) {
                throw new IllegalArgumentException("ChargerOcppId es obligatorio");
            }
        }
    }
}
