package com.casrusil.siierpai.modules.energy.domain.port.out;

import com.casrusil.siierpai.modules.energy.domain.model.StationId;
import com.casrusil.siierpai.modules.energy.domain.model.maintenance.MaintenanceRecord;
import com.casrusil.siierpai.modules.energy.domain.model.maintenance.MaintenanceRecordId;
import com.casrusil.siierpai.modules.energy.domain.model.maintenance.MaintenanceStatus;
import com.casrusil.siierpai.modules.energy.domain.model.maintenance.MaintenanceType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository port for MaintenanceRecord persistence.
 */
public interface MaintenanceRecordRepository {

    /**
     * Saves a maintenance record.
     */
    MaintenanceRecord save(MaintenanceRecord record);

    /**
     * Finds a maintenance record by its ID.
     */
    Optional<MaintenanceRecord> findById(MaintenanceRecordId id);

    /**
     * Finds all maintenance records for a station.
     */
    List<MaintenanceRecord> findByStationId(StationId stationId);

    /**
     * Finds maintenance records by status.
     */
    List<MaintenanceRecord> findByStatus(MaintenanceStatus status);

    /**
     * Finds maintenance records by type.
     */
    List<MaintenanceRecord> findByType(MaintenanceType type);

    /**
     * Finds scheduled maintenance between dates.
     */
    List<MaintenanceRecord> findScheduledBetween(LocalDateTime start, LocalDateTime end);

    /**
     * Finds overdue maintenance records.
     */
    List<MaintenanceRecord> findOverdue();

    /**
     * Deletes a maintenance record.
     */
    void delete(MaintenanceRecordId id);
}
