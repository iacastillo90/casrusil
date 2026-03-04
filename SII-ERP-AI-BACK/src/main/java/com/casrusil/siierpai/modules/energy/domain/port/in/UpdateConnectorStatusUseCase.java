package com.casrusil.siierpai.modules.energy.domain.port.in;

import com.casrusil.siierpai.modules.energy.domain.model.*;

/**
 * Puerto de Entrada: Caso de Uso - Actualizar Estado de Conector.
 * Permite actualizar el estado de un conector (OCPP StatusNotification).
 * 
 * Patrón: Hexagonal Architecture - Input Port (Use Case)
 * Implementación: En application/service
 */
public interface UpdateConnectorStatusUseCase {
    
    /**
     * Actualiza el estado de un conector.
     * Emite: ConnectorStatusChangedEvent
     * 
     * @param command Comando con el nuevo estado
     */
    void execute(UpdateConnectorStatusCommand command);
    
    /**
     * Comando para actualizar estado de conector.
     */
    record UpdateConnectorStatusCommand(
            StationId stationId,
            OcppIdentifier chargerOcppId,
            ConnectorId connectorId,
            ConnectorStatus newStatus
    ) {
        public UpdateConnectorStatusCommand {
            if (stationId == null) {
                throw new IllegalArgumentException("StationId es obligatorio");
            }
            if (chargerOcppId == null) {
                throw new IllegalArgumentException("ChargerOcppId es obligatorio");
            }
            if (connectorId == null) {
                throw new IllegalArgumentException("ConnectorId es obligatorio");
            }
            if (newStatus == null) {
                throw new IllegalArgumentException("El nuevo estado es obligatorio");
            }
        }
    }
}
