package com.casrusil.siierpai.modules.energy.infrastructure.persistence.repository;

import com.casrusil.siierpai.modules.energy.domain.model.maintenance.MaintenanceStatus;
import com.casrusil.siierpai.modules.energy.domain.model.maintenance.MaintenanceType;
import com.casrusil.siierpai.modules.energy.infrastructure.persistence.entity.MaintenanceRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for MaintenanceRecordEntity.
 */
@Repository
public interface JpaMaintenanceRecordRepository extends JpaRepository<MaintenanceRecordEntity, UUID> {

    /**
     * Finds all maintenance records for a specific station.
     */
    List<MaintenanceRecordEntity> findByStationId(UUID stationId);

    /**
     * Finds all maintenance records with a specific status.
     */
    List<MaintenanceRecordEntity> findByStatus(MaintenanceStatus status);

    /**
     * Finds all maintenance records of a specific type.
     */
    List<MaintenanceRecordEntity> findByType(MaintenanceType type);

    /**
     * Finds scheduled maintenance between two dates.
     */
    @Query("SELECT m FROM MaintenanceRecordEntity m WHERE m.status = 'SCHEDULED' AND m.scheduledDate BETWEEN :start AND :end ORDER BY m.scheduledDate ASC")
    List<MaintenanceRecordEntity> findScheduledBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    /**
     * Finds all overdue maintenance records (scheduled in the past but not
     * completed).
     */
    @Query("SELECT m FROM MaintenanceRecordEntity m WHERE m.status = 'SCHEDULED' AND m.scheduledDate < CURRENT_TIMESTAMP ORDER BY m.scheduledDate ASC")
    List<MaintenanceRecordEntity> findOverdue();
}
