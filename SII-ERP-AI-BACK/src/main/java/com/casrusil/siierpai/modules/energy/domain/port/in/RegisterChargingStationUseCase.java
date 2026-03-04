package com.casrusil.siierpai.modules.energy.domain.port.in;

import com.casrusil.siierpai.modules.energy.domain.model.*;

import java.util.UUID;

/**
 * Puerto de Entrada: Caso de Uso - Registrar Estación de Carga.
 * Permite registrar una nueva estación de carga en el sistema.
 * 
 * Patrón: Hexagonal Architecture - Input Port (Use Case)
 * Implementación: En application/service
 */
public interface RegisterChargingStationUseCase {

    /**
     * Registra una nueva estación de carga.
     * 
     * @param command Comando con los datos de la estación
     * @return La estación registrada
     */
    ChargingStation execute(RegisterStationCommand command);

    /**
     * Comando para registrar una estación.
     */
    record RegisterStationCommand(
            String name,
            StationLocation location,
            UUID companyId) {
        public RegisterStationCommand {
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("El nombre es obligatorio");
            }
            if (location == null) {
                throw new IllegalArgumentException("La ubicación es obligatoria");
            }
            if (companyId == null) {
                throw new IllegalArgumentException("El ID de empresa es obligatorio");
            }
        }
    }
}
