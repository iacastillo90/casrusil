package com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.rest;

import com.casrusil.siierpai.modules.energy.application.service.ChargingStationService;
import com.casrusil.siierpai.modules.energy.application.service.maintenance.MaintenanceService;
import com.casrusil.siierpai.modules.energy.domain.model.StationId;
import com.casrusil.siierpai.modules.energy.domain.model.maintenance.MaintenanceRecord;
import com.casrusil.siierpai.modules.energy.domain.model.maintenance.MaintenanceRecordId;
import com.casrusil.siierpai.modules.energy.domain.model.maintenance.MaintenanceStatus;
import com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.rest.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST Controller for maintenance operations.
 * Provides endpoints for scheduling, starting, completing, and querying
 * maintenance tasks.
 */
// P0 multiempresa: apagado por defecto porque sus tablas (alertas, mantenciones, precios, campañas, catálogo) no tienen company_id.
// Sin el bean, sus rutas responden 404 como cualquier ruta inexistente. Ver ronda P6.
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(prefix = "app.features.energy-admin", name = "enabled", havingValue = "true", matchIfMissing = false)
@RestController
@RequestMapping("/api/v1/maintenance")
@Tag(name = "Maintenance", description = "Charging station maintenance management")
@PreAuthorize("hasAnyRole('ADMIN','OPERATIONS')")
public class MaintenanceController {

    private static final Logger logger = LoggerFactory.getLogger(MaintenanceController.class);

    private final MaintenanceService maintenanceService;
    private final ChargingStationService stationService;

    public MaintenanceController(
            MaintenanceService maintenanceService,
            ChargingStationService stationService) {
        this.maintenanceService = maintenanceService;
        this.stationService = stationService;
    }

    /**
     * Schedules a new maintenance task for a charging station.
     */
    @PostMapping("/tasks")
    @Operation(summary = "Schedule maintenance", description = "Creates a new scheduled maintenance task for a charging station")
    public ResponseEntity<MaintenanceRecordDto> scheduleMaintenance(
            @RequestBody @Valid ScheduleMaintenanceRequest request) {

        logger.info("Scheduling {} maintenance for station {} on {}",
                request.type(), request.stationId(), request.scheduledDate());

        MaintenanceRecord record = maintenanceService.scheduleMaintenance(
                StationId.of(request.stationId()),
                request.type(),
                request.scheduledDate(),
                request.description());

        return ResponseEntity.ok(MaintenanceRecordDto.from(record));
    }

    /**
     * Starts a scheduled maintenance task.
     */
    @PutMapping("/tasks/{id}/start")
    @Operation(summary = "Start maintenance", description = "Marks a scheduled maintenance task as in progress and updates station status")
    public ResponseEntity<MaintenanceRecordDto> startMaintenance(
            @PathVariable UUID id,
            @RequestBody @Valid StartMaintenanceRequest request) {

        logger.info("Starting maintenance {} with technician {}", id, request.technician());

        MaintenanceRecord record = maintenanceService.startMaintenance(
                MaintenanceRecordId.of(id),
                request.technician());

        // Update station status to UNAVAILABLE
        try {
            stationService.markStationUnderMaintenance(record.getStationId());
        } catch (Exception e) {
            logger.warn("Could not update station status for {}: {}", record.getStationId(), e.getMessage());
            // Continue anyway - maintenance record is more important
        }

        return ResponseEntity.ok(MaintenanceRecordDto.from(record));
    }

    /**
     * Completes a maintenance task.
     */
    @PutMapping("/tasks/{id}/complete")
    @Operation(summary = "Complete maintenance", description = "Marks a maintenance task as completed and restores station availability")
    public ResponseEntity<MaintenanceRecordDto> completeMaintenance(
            @PathVariable UUID id,
            @RequestBody @Valid CompleteMaintenanceRequest request) {

        logger.info("Completing maintenance {}", id);

        MaintenanceRecord record = maintenanceService.completeMaintenance(
                MaintenanceRecordId.of(id),
                request.notes(),
                request.cost());

        // Restore station status to AVAILABLE
        try {
            stationService.markStationAvailable(record.getStationId());
        } catch (Exception e) {
            logger.warn("Could not restore station status for {}: {}", record.getStationId(), e.getMessage());
            // Continue anyway - maintenance record is more important
        }

        return ResponseEntity.ok(MaintenanceRecordDto.from(record));
    }

    /**
     * Cancels a maintenance task.
     */
    @PutMapping("/tasks/{id}/cancel")
    @Operation(summary = "Cancel maintenance", description = "Cancels a scheduled or in-progress maintenance task")
    public ResponseEntity<MaintenanceRecordDto> cancelMaintenance(
            @PathVariable UUID id,
            @RequestParam String reason) {

        logger.info("Cancelling maintenance {}: {}", id, reason);

        MaintenanceRecord record = maintenanceService.cancelMaintenance(
                MaintenanceRecordId.of(id),
                reason);

        return ResponseEntity.ok(MaintenanceRecordDto.from(record));
    }

    /**
     * Marks a maintenance task as failed.
     */
    @PutMapping("/tasks/{id}/fail")
    @Operation(summary = "Mark maintenance as failed", description = "Marks an in-progress maintenance task as failed")
    public ResponseEntity<MaintenanceRecordDto> failMaintenance(
            @PathVariable UUID id,
            @RequestParam String notes) {

        logger.warn("Marking maintenance {} as failed: {}", id, notes);

        MaintenanceRecord record = maintenanceService.failMaintenance(
                MaintenanceRecordId.of(id),
                notes);

        // Restore station status if maintenance failed
        try {
            stationService.markStationAvailable(record.getStationId());
        } catch (Exception e) {
            logger.warn("Could not restore station status for {}: {}", record.getStationId(), e.getMessage());
        }

        return ResponseEntity.ok(MaintenanceRecordDto.from(record));
    }

    /**
     * Gets maintenance tasks filtered by station or status.
     */
    @GetMapping("/tasks")
    @Operation(summary = "List maintenance tasks", description = "Retrieves maintenance tasks filtered by station ID or status")
    public ResponseEntity<List<MaintenanceRecordDto>> getMaintenanceTasks(
            @RequestParam(required = false) UUID stationId,
            @RequestParam(required = false) MaintenanceStatus status) {

        List<MaintenanceRecord> records;

        if (stationId != null) {
            logger.debug("Fetching maintenance history for station {}", stationId);
            records = maintenanceService.getMaintenanceHistory(StationId.of(stationId));
        } else if (status != null) {
            logger.debug("Fetching maintenance tasks with status {}", status);
            records = maintenanceService.getMaintenanceByStatus(status);
        } else {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(records.stream()
                .map(MaintenanceRecordDto::from)
                .toList());
    }

    /**
     * Gets upcoming scheduled maintenance.
     */
    @GetMapping("/tasks/upcoming")
    @Operation(summary = "Get upcoming maintenance", description = "Retrieves maintenance tasks scheduled within the next N days")
    public ResponseEntity<List<MaintenanceRecordDto>> getUpcomingMaintenance(
            @RequestParam(defaultValue = "7") int days) {

        logger.debug("Fetching upcoming maintenance for next {} days", days);

        List<MaintenanceRecord> records = maintenanceService.getUpcomingMaintenance(days);
        return ResponseEntity.ok(records.stream()
                .map(MaintenanceRecordDto::from)
                .toList());
    }

    /**
     * Gets overdue maintenance tasks.
     */
    @GetMapping("/tasks/overdue")
    @Operation(summary = "Get overdue maintenance", description = "Retrieves maintenance tasks that are past their scheduled date")
    public ResponseEntity<List<MaintenanceRecordDto>> getOverdueMaintenance() {

        logger.debug("Fetching overdue maintenance tasks");

        List<MaintenanceRecord> records = maintenanceService.getOverdueMaintenance();
        return ResponseEntity.ok(records.stream()
                .map(MaintenanceRecordDto::from)
                .toList());
    }

    /**
     * Gets maintenance summary statistics for a station.
     */
    @GetMapping("/summary/{stationId}")
    @Operation(summary = "Get maintenance summary", description = "Retrieves maintenance statistics and summary for a specific station")
    public ResponseEntity<MaintenanceSummaryDto> getMaintenanceSummary(
            @PathVariable UUID stationId) {

        logger.debug("Fetching maintenance summary for station {}", stationId);

        var summary = maintenanceService.getMaintenanceSummary(StationId.of(stationId));
        return ResponseEntity.ok(MaintenanceSummaryDto.from(summary));
    }
}
