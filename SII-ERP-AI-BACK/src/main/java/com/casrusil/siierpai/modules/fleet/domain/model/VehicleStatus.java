package com.casrusil.siierpai.modules.fleet.domain.model;

/**
 * Estado de un vehículo en la flota.
 */
public enum VehicleStatus {
    AVAILABLE, // Disponible para uso
    CHARGING, // En proceso de carga
    IN_USE, // En uso (conduciendo)
    OUT_OF_SERVICE, // Fuera de servicio (mantenimiento)
    RESERVED // Reservado
}
