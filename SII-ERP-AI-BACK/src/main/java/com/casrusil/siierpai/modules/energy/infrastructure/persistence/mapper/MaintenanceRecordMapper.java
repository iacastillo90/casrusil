package com.casrusil.siierpai.modules.energy.infrastructure.persistence.mapper;

import com.casrusil.siierpai.modules.energy.domain.model.StationId;
import com.casrusil.siierpai.modules.energy.domain.model.maintenance.MaintenanceRecord;
import com.casrusil.siierpai.modules.energy.domain.model.maintenance.MaintenanceRecordId;
import com.casrusil.siierpai.modules.energy.infrastructure.persistence.entity.MaintenanceRecordEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between MaintenanceRecord domain model and
 * MaintenanceRecordEntity JPA entity.
 */
@Component
public class MaintenanceRecordMapper {

    /**
     * Converts domain model to JPA entity.
     */
    public MaintenanceRecordEntity toEntity(MaintenanceRecord record) {
        MaintenanceRecordEntity entity = new MaintenanceRecordEntity();
        entity.setId(record.getId().getValue());
        entity.setStationId(record.getStationId().getValue());
        entity.setType(record.getType());
        entity.setStatus(record.getStatus());
        entity.setScheduledDate(record.getScheduledDate());
        entity.setStartedAt(record.getStartedAt());
        entity.setCompletedAt(record.getCompletedAt());
        entity.setDescription(record.getDescription());
        entity.setTechnician(record.getTechnician());
        entity.setNotes(record.getNotes());
        entity.setCost(record.getCost());
        entity.setCreatedAt(record.getCreatedAt());
        entity.setUpdatedAt(record.getUpdatedAt());
        return entity;
    }

    /**
     * Converts JPA entity to domain model.
     */
    public MaintenanceRecord toDomain(MaintenanceRecordEntity entity) {
        return MaintenanceRecord.builder()
                .id(MaintenanceRecordId.of(entity.getId()))
                .stationId(StationId.of(entity.getStationId()))
                .type(entity.getType())
                .status(entity.getStatus())
                .scheduledDate(entity.getScheduledDate())
                .startedAt(entity.getStartedAt())
                .completedAt(entity.getCompletedAt())
                .description(entity.getDescription())
                .technician(entity.getTechnician())
                .notes(entity.getNotes())
                .cost(entity.getCost())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
