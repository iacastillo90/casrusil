package com.casrusil.siierpai.modules.energy.application.listener;

import com.casrusil.siierpai.modules.energy.domain.event.MaintenanceEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Event listener for maintenance notifications.
 * Logs maintenance events and can be extended to send email/SMS notifications.
 */
@Component
public class MaintenanceNotificationListener {

    private static final Logger logger = LoggerFactory.getLogger(MaintenanceNotificationListener.class);

    @EventListener
    public void onMaintenanceScheduled(MaintenanceEvent.MaintenanceScheduled event) {
        logger.info("🔧 Maintenance scheduled for station {} on {} - Type: {}, Description: {}",
                event.stationId(),
                event.scheduledDate(),
                event.type(),
                event.description());

        // TODO: Send email notification to maintenance team
        // TODO: Create calendar event
    }

    @EventListener
    public void onMaintenanceStarted(MaintenanceEvent.MaintenanceStarted event) {
        logger.info("⚙️  Maintenance started for station {} by {} at {}",
                event.stationId(),
                event.technician(),
                event.startedAt());

        // TODO: Send SMS to operations manager
        // TODO: Update dashboard status
    }

    @EventListener
    public void onMaintenanceCompleted(MaintenanceEvent.MaintenanceCompleted event) {
        logger.info("✅ Maintenance completed for station {} at {} - Cost: ${}, Notes: {}",
                event.stationId(),
                event.completedAt(),
                event.cost(),
                event.notes());

        // TODO: Send completion report via email
        // TODO: Update maintenance history dashboard
    }

    @EventListener
    public void onMaintenanceFailed(MaintenanceEvent.MaintenanceFailed event) {
        logger.error("❌ Maintenance FAILED for station {} at {} - Reason: {}",
                event.stationId(),
                event.failedAt(),
                event.reason());

        // TODO: Send urgent notification to maintenance supervisor
        // TODO: Create follow-up task
    }

    @EventListener
    public void onMaintenanceCancelled(MaintenanceEvent.MaintenanceCancelled event) {
        logger.warn("🚫 Maintenance CANCELLED for station {} at {} - Reason: {}",
                event.stationId(),
                event.cancelledAt(),
                event.reason());

        // TODO: Notify scheduled technician
        // TODO: Update resource allocation
    }

    @EventListener
    public void onMaintenanceOverdue(MaintenanceEvent.MaintenanceOverdue event) {
        logger.warn("⚠️  Maintenance OVERDUE for station {} - Scheduled: {}, Detected: {}",
                event.stationId(),
                event.scheduledDate(),
                event.detectedAt());

        // TODO: Send urgent alert to operations manager
        // TODO: Escalate to management if overdue > 24 hours
        // TODO: Auto-reschedule if possible
    }
}
