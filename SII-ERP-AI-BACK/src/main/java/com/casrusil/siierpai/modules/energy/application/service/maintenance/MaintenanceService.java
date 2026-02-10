package com.casrusil.siierpai.modules.energy.application.service.maintenance;

import com.casrusil.siierpai.modules.energy.domain.event.MaintenanceEvent;
import com.casrusil.siierpai.modules.energy.domain.model.StationId;
import com.casrusil.siierpai.modules.energy.domain.model.maintenance.MaintenanceRecord;
import com.casrusil.siierpai.modules.energy.domain.model.maintenance.MaintenanceRecordId;
import com.casrusil.siierpai.modules.energy.domain.model.maintenance.MaintenanceStatus;
import com.casrusil.siierpai.modules.energy.domain.model.maintenance.MaintenanceType;
import com.casrusil.siierpai.modules.energy.domain.port.out.MaintenanceRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for managing charger maintenance.
 */
@Service
@Transactional
public class MaintenanceService {

    private static final Logger logger = LoggerFactory.getLogger(MaintenanceService.class);

    private final MaintenanceRecordRepository maintenanceRecordRepository;
    private final ApplicationEventPublisher eventPublisher;

    public MaintenanceService(
            MaintenanceRecordRepository maintenanceRecordRepository,
            ApplicationEventPublisher eventPublisher) {
        this.maintenanceRecordRepository = maintenanceRecordRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Schedules maintenance for a station.
     */
    public MaintenanceRecord scheduleMaintenance(
            StationId stationId,
            MaintenanceType type,
            LocalDateTime scheduledDate,
            String description) {

        logger.info("Scheduling {} maintenance for station {} on {}",
                type, stationId, scheduledDate);

        MaintenanceRecord record = MaintenanceRecord.schedule(
                stationId,
                type,
                scheduledDate,
                description);

        MaintenanceRecord saved = maintenanceRecordRepository.save(record);

        // Publish event
        eventPublisher.publishEvent(new MaintenanceEvent.MaintenanceScheduled(
                saved.getId(),
                saved.getStationId(),
                saved.getType(),
                saved.getScheduledDate(),
                saved.getDescription()));

        return saved;
    }

    /**
     * Starts a maintenance task.
     */
    public MaintenanceRecord startMaintenance(MaintenanceRecordId id, String technician) {
        logger.info("Starting maintenance {} with technician {}", id, technician);

        MaintenanceRecord record = maintenanceRecordRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Maintenance record not found: " + id));

        record.start(technician);
        MaintenanceRecord saved = maintenanceRecordRepository.save(record);

        // Publish event
        eventPublisher.publishEvent(new MaintenanceEvent.MaintenanceStarted(
                saved.getId(),
                saved.getStationId(),
                saved.getTechnician(),
                saved.getStartedAt()));

        return saved;
    }

    /**
     * Completes a maintenance task.
     */
    public MaintenanceRecord completeMaintenance(
            MaintenanceRecordId id,
            String notes,
            BigDecimal cost) {

        logger.info("Completing maintenance {}", id);

        MaintenanceRecord record = maintenanceRecordRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Maintenance record not found: " + id));

        record.complete(notes, cost);
        MaintenanceRecord saved = maintenanceRecordRepository.save(record);

        // Publish event
        eventPublisher.publishEvent(new MaintenanceEvent.MaintenanceCompleted(
                saved.getId(),
                saved.getStationId(),
                saved.getCost(),
                saved.getNotes(),
                saved.getCompletedAt()));

        return saved;
    }

    /**
     * Marks maintenance as failed.
     */
    public MaintenanceRecord failMaintenance(MaintenanceRecordId id, String notes) {
        logger.warn("Marking maintenance {} as failed", id);

        MaintenanceRecord record = maintenanceRecordRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Maintenance record not found: " + id));

        record.fail(notes);
        MaintenanceRecord saved = maintenanceRecordRepository.save(record);

        // Publish event
        eventPublisher.publishEvent(new MaintenanceEvent.MaintenanceFailed(
                saved.getId(),
                saved.getStationId(),
                saved.getNotes(),
                saved.getCompletedAt()));

        return saved;
    }

    /**
     * Cancels a maintenance task.
     */
    public MaintenanceRecord cancelMaintenance(MaintenanceRecordId id, String reason) {
        logger.info("Cancelling maintenance {}: {}", id, reason);

        MaintenanceRecord record = maintenanceRecordRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Maintenance record not found: " + id));

        record.cancel(reason);
        MaintenanceRecord saved = maintenanceRecordRepository.save(record);

        // Publish event
        eventPublisher.publishEvent(new MaintenanceEvent.MaintenanceCancelled(
                saved.getId(),
                saved.getStationId(),
                saved.getNotes(),
                saved.getUpdatedAt()));

        return saved;
    }

    /**
     * Gets maintenance history for a station.
     */
    @Transactional(readOnly = true)
    public List<MaintenanceRecord> getMaintenanceHistory(StationId stationId) {
        return maintenanceRecordRepository.findByStationId(stationId);
    }

    /**
     * Gets upcoming scheduled maintenance.
     */
    @Transactional(readOnly = true)
    public List<MaintenanceRecord> getUpcomingMaintenance(int days) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime future = now.plusDays(days);

        return maintenanceRecordRepository.findScheduledBetween(now, future);
    }

    /**
     * Gets overdue maintenance tasks.
     */
    @Transactional(readOnly = true)
    public List<MaintenanceRecord> getOverdueMaintenance() {
        List<MaintenanceRecord> overdue = maintenanceRecordRepository.findOverdue();

        // Publish events for newly detected overdue maintenance
        overdue.forEach(record -> {
            eventPublisher.publishEvent(new MaintenanceEvent.MaintenanceOverdue(
                    record.getId(),
                    record.getStationId(),
                    record.getType(),
                    record.getScheduledDate(),
                    LocalDateTime.now()));
        });

        return overdue;
    }

    /**
     * Gets maintenance records by status.
     */
    @Transactional(readOnly = true)
    public List<MaintenanceRecord> getMaintenanceByStatus(MaintenanceStatus status) {
        return maintenanceRecordRepository.findByStatus(status);
    }

    /**
     * Generates maintenance summary for a station.
     */
    @Transactional(readOnly = true)
    public MaintenanceSummary getMaintenanceSummary(StationId stationId) {
        List<MaintenanceRecord> records = maintenanceRecordRepository.findByStationId(stationId);

        long totalMaintenance = records.size();
        long completedMaintenance = records.stream()
                .filter(r -> r.getStatus() == MaintenanceStatus.COMPLETED)
                .count();

        BigDecimal totalCost = records.stream()
                .filter(r -> r.getCost() != null)
                .map(MaintenanceRecord::getCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        LocalDateTime lastMaintenance = records.stream()
                .filter(r -> r.getStatus() == MaintenanceStatus.COMPLETED)
                .map(MaintenanceRecord::getCompletedAt)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        return new MaintenanceSummary(
                stationId,
                totalMaintenance,
                completedMaintenance,
                totalCost,
                lastMaintenance);
    }

    /**
     * Maintenance summary DTO.
     */
    public record MaintenanceSummary(
            StationId stationId,
            long totalMaintenanceCount,
            long completedMaintenanceCount,
            BigDecimal totalCost,
            LocalDateTime lastMaintenanceDate) {
    }
}
