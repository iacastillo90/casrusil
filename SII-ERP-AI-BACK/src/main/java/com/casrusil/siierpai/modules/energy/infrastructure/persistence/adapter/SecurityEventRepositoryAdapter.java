package com.casrusil.siierpai.modules.energy.infrastructure.persistence.adapter;

import com.casrusil.siierpai.modules.energy.domain.model.StationId;
import com.casrusil.siierpai.modules.energy.domain.model.security.SecurityEvent;
import com.casrusil.siierpai.modules.energy.domain.port.out.SecurityEventRepository;
import com.casrusil.siierpai.modules.energy.infrastructure.persistence.entity.SecurityEventEntity;
import com.casrusil.siierpai.modules.energy.infrastructure.persistence.repository.SecurityEventJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * JPA Adapter for SecurityEventRepository.
 * 
 * <p>
 * Implements the hexagonal architecture port by adapting between
 * the domain model and JPA entities.
 * 
 * @since 1.0
 */
@Component
@RequiredArgsConstructor
public class SecurityEventRepositoryAdapter implements SecurityEventRepository {

    private final SecurityEventJpaRepository jpaRepository;

    @Override
    public SecurityEvent save(SecurityEvent event) {
        SecurityEventEntity entity = toEntity(event);
        SecurityEventEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public List<SecurityEvent> findByStationId(StationId stationId) {
        return jpaRepository.findByStationId(stationId.getValue()).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<SecurityEvent> findByEventType(String eventType) {
        return jpaRepository.findByEventType(eventType).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<SecurityEvent> findByTimestampBetween(LocalDateTime start, LocalDateTime end) {
        return jpaRepository.findByTimestampBetween(start, end).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<SecurityEvent> findByStationIdAndTimestampBetween(
            StationId stationId,
            LocalDateTime start,
            LocalDateTime end) {
        return jpaRepository.findByStationIdAndTimestampBetween(
                stationId.getValue(),
                start,
                end).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteOlderThan(LocalDateTime timestamp) {
        jpaRepository.deleteOlderThan(timestamp);
    }

    // Mapping methods
    private SecurityEventEntity toEntity(SecurityEvent event) {
        return SecurityEventEntity.builder()
                .id(UUID.randomUUID())
                .stationId(event.getStationId().getValue())
                .eventType(event.getEventType())
                .timestamp(event.getTimestamp())
                .techInfo(event.getTechInfo())
                .build();
    }

    private SecurityEvent toDomain(SecurityEventEntity entity) {
        return SecurityEvent.builder()
                .stationId(StationId.of(entity.getStationId()))
                .eventType(entity.getEventType())
                .timestamp(entity.getTimestamp())
                .techInfo(entity.getTechInfo())
                .build();
    }
}
