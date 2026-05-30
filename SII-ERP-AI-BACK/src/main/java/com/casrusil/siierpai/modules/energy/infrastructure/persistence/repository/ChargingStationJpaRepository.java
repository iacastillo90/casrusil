package com.casrusil.siierpai.modules.energy.infrastructure.persistence.repository;

import com.casrusil.siierpai.modules.energy.domain.model.StationStatus;
import com.casrusil.siierpai.modules.energy.infrastructure.persistence.entity.ChargingStationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA Repository: Estaciones de Carga.
 * Proporciona acceso a datos para ChargingStationEntity.
 */
@Repository
public interface ChargingStationJpaRepository extends JpaRepository<ChargingStationEntity, UUID> {

    /**
     * Busca estaciones por empresa.
     */
    List<ChargingStationEntity> findByCompanyId(UUID companyId);

    /**
     * Busca estaciones por estado.
     */
    List<ChargingStationEntity> findByStatus(StationStatus status);

    /**
     * Busca estaciones operativas.
     */
    default List<ChargingStationEntity> findAllOperational() {
        return findByStatus(StationStatus.OPERATIONAL);
    }

    /**
     * Busca estaciones con al menos un conector disponible.
     */
    @Query("""
            SELECT DISTINCT s FROM ChargingStationEntity s
            JOIN s.chargers c
            JOIN c.connectors conn
            WHERE conn.status = 'AVAILABLE' AND conn.active = true
            """)
    List<ChargingStationEntity> findStationsWithAvailableConnectors();

    /**
     * Busca estaciones cercanas a una ubicación (implementación básica).
     * En producción, usar PostGIS o similar para búsqueda geoespacial eficiente.
     */
    @Query("""
            SELECT s FROM ChargingStationEntity s
            WHERE s.location.latitude BETWEEN :minLat AND :maxLat
            AND s.location.longitude BETWEEN :minLon AND :maxLon
            """)
    List<ChargingStationEntity> findNearby(Double minLat, Double maxLat, Double minLon, Double maxLon);
}
