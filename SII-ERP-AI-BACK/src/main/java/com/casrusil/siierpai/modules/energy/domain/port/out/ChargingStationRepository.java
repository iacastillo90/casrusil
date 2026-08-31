package com.casrusil.siierpai.modules.energy.domain.port.out;

import com.casrusil.siierpai.modules.energy.domain.model.*;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de Salida: Repositorio de Estaciones de Carga.
 * Define las operaciones de persistencia sin conocer la implementación (JPA,
 * MongoDB, etc.).
 * 
 * Patrón: Hexagonal Architecture - Output Port (Repository Pattern)
 * Implementación: En infrastructure/persistence
 */
public interface ChargingStationRepository {

    /**
     * Guarda o actualiza una estación de carga.
     */
    ChargingStation save(ChargingStation station);

    /**
     * Busca una estación por su ID.
     */
    Optional<ChargingStation> findById(StationId stationId);

    /**
     * Busca un cargador por su OCPP ID en todas las estaciones.
     */
    Optional<Charger> findChargerByOcppId(OcppIdentifier ocppId);

    /**
     * Busca todas las estaciones de una empresa.
     */
    List<ChargingStation> findByCompanyId(java.util.UUID companyId);

    /**
     * Busca todas las estaciones operativas.
     */
    List<ChargingStation> findAllOperational();

    /**
     * Busca estaciones con conectores disponibles.
     */
    List<ChargingStation> findStationsWithAvailableConnectors();

    /**
     * Busca estaciones cercanas a una ubicación (radio en km).
     */
    List<ChargingStation> findNearby(Double latitude, Double longitude, Double radiusKm);

    /**
     * Elimina una estación (soft delete recomendado).
     */
    void delete(StationId stationId);

    /**
     * Verifica si existe una estación con el ID dado.
     */
    boolean existsById(StationId stationId);

    /**
     * Cuenta el total de estaciones.
     */
    long count();

    /**
     * Cuenta estaciones por estado.
     */
    long countByStatus(StationStatus status);
}
