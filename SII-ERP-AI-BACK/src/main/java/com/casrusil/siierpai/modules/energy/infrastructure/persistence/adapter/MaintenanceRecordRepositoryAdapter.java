package com.casrusil.siierpai.modules.energy.infrastructure.persistence.adapter;

import com.casrusil.siierpai.modules.energy.domain.model.StationId;
import com.casrusil.siierpai.modules.energy.domain.model.maintenance.MaintenanceRecord;
import com.casrusil.siierpai.modules.energy.domain.model.maintenance.MaintenanceRecordId;
import com.casrusil.siierpai.modules.energy.domain.model.maintenance.MaintenanceStatus;
import com.casrusil.siierpai.modules.energy.domain.model.maintenance.MaintenanceType;
import com.casrusil.siierpai.modules.energy.domain.port.out.MaintenanceRecordRepository;
import com.casrusil.siierpai.modules.energy.infrastructure.persistence.entity.MaintenanceRecordEntity;
import com.casrusil.siierpai.modules.energy.infrastructure.persistence.mapper.MaintenanceRecordMapper;
import com.casrusil.siierpai.modules.energy.infrastructure.persistence.repository.JpaMaintenanceRecordRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Adapter implementing the MaintenanceRecordRepository port using JPA.
 */
@Component
public class MaintenanceRecordRepositoryAdapter implements MaintenanceRecordRepository {

    private final JpaMaintenanceRecordRepository jpaRepository;
    private final MaintenanceRecordMapper mapper;

    public MaintenanceRecordRepositoryAdapter(
            JpaMaintenanceRecordRepository jpaRepository,
            MaintenanceRecordMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public MaintenanceRecord save(MaintenanceRecord record) {
        MaintenanceRecordEntity entity = mapper.toEntity(record);
        MaintenanceRecordEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<MaintenanceRecord> findById(MaintenanceRecordId id) {
        return jpaRepository.findById(id.getValue())
                .map(mapper::toDomain);
    }

    @Override
    public List<MaintenanceRecord> findByStationId(StationId stationId) {
        return jpaRepository.findByStationId(stationId.getValue()).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<MaintenanceRecord> findByStatus(MaintenanceStatus status) {
        return jpaRepository.findByStatus(status).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<MaintenanceRecord> findByType(MaintenanceType type) {
        return jpaRepository.findByType(type).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<MaintenanceRecord> findScheduledBetween(LocalDateTime start, LocalDateTime end) {
        return jpaRepository.findScheduledBetween(start, end).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<MaintenanceRecord> findOverdue() {
        return jpaRepository.findOverdue().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public void delete(MaintenanceRecordId id) {
        jpaRepository.deleteById(id.getValue());
    }
}
