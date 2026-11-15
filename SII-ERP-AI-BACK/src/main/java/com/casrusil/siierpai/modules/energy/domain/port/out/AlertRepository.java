package com.casrusil.siierpai.modules.energy.domain.port.out;

import com.casrusil.siierpai.modules.energy.domain.model.monitoring.Alert;
import com.casrusil.siierpai.modules.energy.domain.model.monitoring.AlertId;
import com.casrusil.siierpai.modules.energy.domain.model.monitoring.AlertSeverity;
import com.casrusil.siierpai.modules.energy.domain.model.StationId;

import java.util.List;
import java.util.Optional;

public interface AlertRepository {
    Alert save(Alert alert);
    Optional<Alert> findById(AlertId id);
    List<Alert> findAll();
    List<Alert> findActive();
    List<Alert> findUnacknowledged();
    List<Alert> findBySeverity(AlertSeverity severity);
    List<Alert> findByStation(StationId stationId);
    long count();
    long countActive();
    long countCritical();
    long countUnacknowledged();
}
