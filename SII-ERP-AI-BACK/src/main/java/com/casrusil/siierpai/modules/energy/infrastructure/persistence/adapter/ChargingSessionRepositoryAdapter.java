package com.casrusil.siierpai.modules.energy.infrastructure.persistence.adapter;

import com.casrusil.siierpai.modules.energy.domain.model.*;
import com.casrusil.siierpai.modules.energy.domain.port.out.ChargingSessionRepository;
import com.casrusil.siierpai.modules.energy.infrastructure.persistence.mapper.ChargingSessionMapper;
import com.casrusil.siierpai.modules.energy.infrastructure.persistence.repository.ChargingSessionJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adapter: Implementación del puerto ChargingSessionRepository usando Spring
 * Data JPA.
 * 
 * Patrón: Hexagonal Architecture - Adapter (Output)
 */
@Component
@Transactional
public class ChargingSessionRepositoryAdapter implements ChargingSessionRepository {

    private final ChargingSessionJpaRepository jpaRepository;
    private final ChargingSessionMapper mapper;

    public ChargingSessionRepositoryAdapter(
            ChargingSessionJpaRepository jpaRepository,
            ChargingSessionMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public ChargingSession save(ChargingSession session) {
        var entity = mapper.toEntity(session);
        var saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<ChargingSession> findById(SessionId sessionId) {
        return jpaRepository.findById(sessionId.getValue())
                .map(mapper::toDomain);
    }

    @Override
    public Optional<ChargingSession> findActiveSessionByConnector(
            StationId stationId,
            OcppIdentifier chargerOcppId,
            ConnectorId connectorId) {
        return jpaRepository.findActiveSessionByConnector(
                stationId.getValue(),
                chargerOcppId.getValue(),
                connectorId.getValue()).map(mapper::toDomain);
    }

    @Override
    public List<ChargingSession> findActiveSessionsByStation(StationId stationId) {
        return jpaRepository.findActiveSessionsByStation(stationId.getValue()).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ChargingSession> findByUserId(UUID userId) {
        return jpaRepository.findByUserIdOrderByStartedAtDesc(userId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ChargingSession> findCompletedBetween(LocalDateTime start, LocalDateTime end) {
        return jpaRepository.findByStatusAndStartedAtBetween(
                ChargingSessionStatus.COMPLETED,
                start,
                end).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ChargingSession> findByStationAndDateRange(
            StationId stationId,
            LocalDateTime start,
            LocalDateTime end) {
        return jpaRepository.findByStationIdAndStartedAtBetween(
                stationId.getValue(),
                start,
                end).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public long countActiveSessionsByStation(StationId stationId) {
        return jpaRepository.countActiveSessionsByStation(stationId.getValue());
    }

    @Override
    public boolean hasActiveSession(StationId stationId, OcppIdentifier chargerOcppId, ConnectorId connectorId) {
        return jpaRepository.hasActiveSession(
                stationId.getValue(),
                chargerOcppId.getValue(),
                connectorId.getValue());
    }
}
