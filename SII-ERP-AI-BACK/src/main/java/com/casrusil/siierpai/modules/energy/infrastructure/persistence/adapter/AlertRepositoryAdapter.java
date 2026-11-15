package com.casrusil.siierpai.modules.energy.infrastructure.persistence.adapter;

import com.casrusil.siierpai.modules.energy.domain.model.StationId;
import com.casrusil.siierpai.modules.energy.domain.model.monitoring.Alert;
import com.casrusil.siierpai.modules.energy.domain.model.monitoring.AlertId;
import com.casrusil.siierpai.modules.energy.domain.model.monitoring.AlertSeverity;
import com.casrusil.siierpai.modules.energy.domain.port.out.AlertRepository;
import com.casrusil.siierpai.modules.energy.infrastructure.persistence.mapper.AlertMapper;
import com.casrusil.siierpai.modules.energy.infrastructure.persistence.repository.AlertJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class AlertRepositoryAdapter implements AlertRepository {

    private final AlertJpaRepository jpaRepository;
    private final AlertMapper mapper;

    public AlertRepositoryAdapter(AlertJpaRepository jpaRepository, AlertMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Alert save(Alert alert) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(alert)));
    }

    @Override
    public Optional<Alert> findById(AlertId id) {
        return jpaRepository.findById(id.value()).map(mapper::toDomain);
    }

    @Override
    public List<Alert> findAll() {
        return jpaRepository.findAll().stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Alert> findActive() {
        return jpaRepository.findActive().stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Alert> findUnacknowledged() {
        return jpaRepository.findUnacknowledged().stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Alert> findBySeverity(AlertSeverity severity) {
        return jpaRepository.findBySeverity(severity.name()).stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Alert> findByStation(StationId stationId) {
        return jpaRepository.findByStationId(stationId.getValue()).stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public long count() {
        return jpaRepository.count();
    }

    @Override
    public long countActive() {
        return jpaRepository.countActive();
    }

    @Override
    public long countCritical() {
        return jpaRepository.countCritical();
    }

    @Override
    public long countUnacknowledged() {
        return jpaRepository.countUnacknowledged();
    }
}
