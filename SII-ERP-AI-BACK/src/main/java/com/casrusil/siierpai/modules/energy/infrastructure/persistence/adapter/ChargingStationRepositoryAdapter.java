package com.casrusil.siierpai.modules.energy.infrastructure.persistence.adapter;

import com.casrusil.siierpai.modules.energy.domain.model.*;
import com.casrusil.siierpai.modules.energy.domain.port.out.ChargingStationRepository;
import com.casrusil.siierpai.modules.energy.infrastructure.persistence.entity.ChargingStationEntity;
import com.casrusil.siierpai.modules.energy.infrastructure.persistence.mapper.ChargingStationMapper;
import com.casrusil.siierpai.modules.energy.infrastructure.persistence.repository.ChargerJpaRepository;
import com.casrusil.siierpai.modules.energy.infrastructure.persistence.repository.ChargingStationJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Adapter: Implementación del puerto ChargingStationRepository usando Spring
 * Data JPA.
 * 
 * Patrón: Hexagonal Architecture - Adapter (Output)
 * Responsabilidad: Traducir operaciones de dominio a operaciones JPA.
 */
@Component
@Transactional
public class ChargingStationRepositoryAdapter implements ChargingStationRepository {

    private final ChargingStationJpaRepository jpaRepository;
    private final ChargerJpaRepository chargerJpaRepository;
    private final ChargingStationMapper mapper;

    public ChargingStationRepositoryAdapter(
            ChargingStationJpaRepository jpaRepository,
            ChargerJpaRepository chargerJpaRepository,
            ChargingStationMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.chargerJpaRepository = chargerJpaRepository;
        this.mapper = mapper;
    }

    @Override
    public ChargingStation save(ChargingStation station) {
        ChargingStationEntity entity = mapper.toEntity(station);
        ChargingStationEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<ChargingStation> findById(StationId stationId) {
        return jpaRepository.findById(stationId.getValue())
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Charger> findChargerByOcppId(OcppIdentifier ocppId) {
        return chargerJpaRepository.findByOcppId(ocppId.getValue())
                .map(mapper::toChargerDomain);
    }

    @Override
    public List<ChargingStation> findByCompanyId(java.util.UUID companyId) {
        return jpaRepository.findByCompanyId(companyId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ChargingStation> findAllOperational() {
        return jpaRepository.findAllOperational().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ChargingStation> findStationsWithAvailableConnectors() {
        return jpaRepository.findStationsWithAvailableConnectors().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ChargingStation> findNearby(Double latitude, Double longitude, Double radiusKm) {
        // Cálculo simple de bounding box
        // En producción, usar PostGIS o similar
        double latDelta = radiusKm / 111.0; // 1 grado lat ≈ 111 km
        double lonDelta = radiusKm / (111.0 * Math.cos(Math.toRadians(latitude)));

        double minLat = latitude - latDelta;
        double maxLat = latitude + latDelta;
        double minLon = longitude - lonDelta;
        double maxLon = longitude + lonDelta;

        return jpaRepository.findNearby(minLat, maxLat, minLon, maxLon).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(StationId stationId) {
        jpaRepository.deleteById(stationId.getValue());
    }

    @Override
    public boolean existsById(StationId stationId) {
        return jpaRepository.existsById(stationId.getValue());
    }
}
