package com.casrusil.siierpai.modules.energy.application.listener;

import com.casrusil.siierpai.modules.energy.domain.event.MaintenanceEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import lombok.RequiredArgsConstructor;

/**
 * Event listener for maintenance notifications.
 * Logs maintenance events and sends email notifications.
 */
@Component
@RequiredArgsConstructor
public class MaintenanceNotificationListener {

    private static final Logger logger = LoggerFactory.getLogger(MaintenanceNotificationListener.class);
    private final JavaMailSender mailSender;
    private static final String OPS_EMAIL = "operations@siierp.com";

    @EventListener
    public void onMaintenanceScheduled(MaintenanceEvent.MaintenanceScheduled event) {
        logger.info("🔧 Maintenance scheduled for station {} on {} - Type: {}, Description: {}",
                event.stationId(),
                event.scheduledDate(),
                event.type(),
                event.description());

        sendEmail(
                "Maintenance Scheduled: " + event.stationId(),
                String.format("A maintenance (%s) has been scheduled for %s. Description: %s", 
                        event.type(), event.scheduledDate(), event.description())
        );
    }

    @EventListener
    public void onMaintenanceStarted(MaintenanceEvent.MaintenanceStarted event) {
        logger.info("⚙️  Maintenance started for station {} by {} at {}",
                event.stationId(),
                event.technician(),
                event.startedAt());

        sendEmail(
                "Maintenance Started: " + event.stationId(),
                String.format("Maintenance started by %s at %s.", event.technician(), event.startedAt())
        );
    }

    @EventListener
    public void onMaintenanceCompleted(MaintenanceEvent.MaintenanceCompleted event) {
        logger.info("✅ Maintenance completed for station {} at {} - Cost: ${}, Notes: {}",
                event.stationId(),
                event.completedAt(),
                event.cost(),
                event.notes());

        sendEmail(
                "Maintenance Completed: " + event.stationId(),
                String.format("Maintenance completed at %s. Cost: $%s. Notes: %s", 
                        event.completedAt(), event.cost(), event.notes())
        );
    }

    @EventListener
    public void onMaintenanceFailed(MaintenanceEvent.MaintenanceFailed event) {
        logger.error("❌ Maintenance FAILED for station {} at {} - Reason: {}",
                event.stationId(),
                event.failedAt(),
                event.reason());

        sendEmail(
                "URGENT: Maintenance Failed - " + event.stationId(),
                String.format("Maintenance failed at %s. Reason: %s", event.failedAt(), event.reason())
        );
    }

    @EventListener
    public void onMaintenanceCancelled(MaintenanceEvent.MaintenanceCancelled event) {
        logger.warn("🚫 Maintenance CANCELLED for station {} at {} - Reason: {}",
                event.stationId(),
                event.cancelledAt(),
                event.reason());

        sendEmail(
                "Maintenance Cancelled: " + event.stationId(),
                String.format("Maintenance cancelled at %s. Reason: %s", event.cancelledAt(), event.reason())
        );
    }

    @EventListener
    public void onMaintenanceOverdue(MaintenanceEvent.MaintenanceOverdue event) {
        logger.warn("⚠️  Maintenance OVERDUE for station {} - Scheduled: {}, Detected: {}",
                event.stationId(),
                event.scheduledDate(),
                event.detectedAt());

        sendEmail(
                "URGENT: Maintenance Overdue - " + event.stationId(),
                String.format("Maintenance scheduled for %s is overdue as of %s.", 
                        event.scheduledDate(), event.detectedAt())
        );
    }

    private void sendEmail(String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("system@siierp.com");
            message.setTo(OPS_EMAIL);
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
            logger.debug("Email sent: {}", subject);
        } catch (Exception e) {
            logger.error("Failed to send email notification: {}", e.getMessage());
        }
    }
}
