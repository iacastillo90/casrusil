package com.casrusil.siierpai.modules.energy.infrastructure.persistence.mapper;

import com.casrusil.siierpai.modules.energy.domain.model.SessionId;
import com.casrusil.siierpai.modules.energy.domain.model.StationId;
import com.casrusil.siierpai.modules.energy.domain.model.monitoring.Alert;
import com.casrusil.siierpai.modules.energy.domain.model.monitoring.AlertId;
import com.casrusil.siierpai.modules.energy.domain.model.monitoring.AlertSeverity;
import com.casrusil.siierpai.modules.energy.domain.model.monitoring.AlertType;
import com.casrusil.siierpai.modules.energy.infrastructure.persistence.entity.AlertEntity;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AlertMapper {

    public AlertEntity toEntity(Alert domain) {
        if (domain == null) return null;
        
        AlertEntity entity = new AlertEntity();
        entity.setId(domain.getId().value());
        entity.setType(domain.getType().name());
        entity.setSeverity(domain.getSeverity().name());
        entity.setTitle(domain.getTitle());
        entity.setMessage(domain.getMessage());
        entity.setStationId(domain.getStationId() != null ? domain.getStationId().getValue() : null);
        entity.setSessionId(domain.getSessionId() != null ? domain.getSessionId().getValue() : null);
        entity.setAcknowledged(domain.isAcknowledged());
        entity.setAcknowledgedAt(domain.getAcknowledgedAt());
        entity.setAcknowledgedBy(domain.getAcknowledgedBy());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setResolvedAt(domain.getResolvedAt());
        
        return entity;
    }

    public Alert toDomain(AlertEntity entity) {
        if (entity == null) return null;
        
        return Alert.builder()
                .id(AlertId.of(entity.getId()))
                .type(AlertType.valueOf(entity.getType()))
                .severity(AlertSeverity.valueOf(entity.getSeverity()))
                .title(entity.getTitle())
                .message(entity.getMessage())
                .stationId(entity.getStationId() != null ? StationId.of(entity.getStationId()) : null)
                .sessionId(entity.getSessionId() != null ? SessionId.of(entity.getSessionId()) : null)
                .acknowledged(entity.isAcknowledged())
                .acknowledgedAt(entity.getAcknowledgedAt())
                .acknowledgedBy(entity.getAcknowledgedBy())
                .createdAt(entity.getCreatedAt())
                .resolvedAt(entity.getResolvedAt())
                .build();
    }
}
