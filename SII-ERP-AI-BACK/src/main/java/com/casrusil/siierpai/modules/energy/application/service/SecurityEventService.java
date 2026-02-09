package com.casrusil.siierpai.modules.energy.application.service;

import com.casrusil.siierpai.modules.energy.domain.model.StationId;
import com.casrusil.siierpai.modules.energy.domain.model.security.SecurityEvent;
import com.casrusil.siierpai.modules.energy.domain.port.out.SecurityEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for managing security events in OCPP 1.6 Security Extensions.
 * 
 * Handles logging and querying of security-related events for audit purposes.
 */
@Service
@Transactional
public class SecurityEventService {

    private static final Logger logger = LoggerFactory.getLogger(SecurityEventService.class);

    private final SecurityEventRepository securityEventRepository;

    public SecurityEventService(SecurityEventRepository securityEventRepository) {
        this.securityEventRepository = securityEventRepository;
    }

    /**
     * Logs a security event.
     * 
     * @param eventType Type of security event
     * @param timestamp When the event occurred
     * @param techInfo  Technical information about the event
     * @param stationId Station that reported the event
     */
    public void logSecurityEvent(
            String eventType,
            LocalDateTime timestamp,
            String techInfo,
            StationId stationId) {

        logger.info("Logging security event: {} for station {}", eventType, stationId);

        SecurityEvent event = SecurityEvent.create(
                eventType,
                timestamp,
                techInfo,
                stationId);

        securityEventRepository.save(event);

        // Log critical events at WARN level
        if (isCriticalEvent(eventType)) {
            logger.warn("CRITICAL SECURITY EVENT: {} - Station: {} - Info: {}",
                    eventType, stationId, techInfo);
        }
    }

    /**
     * Gets all security events for a station.
     */
    public List<SecurityEvent> getSecurityEvents(StationId stationId) {
        return securityEventRepository.findByStationId(stationId);
    }

    /**
     * Gets security events within a time range.
     */
    public List<SecurityEvent> getSecurityEvents(LocalDateTime start, LocalDateTime end) {
        return securityEventRepository.findByTimestampBetween(start, end);
    }

    /**
     * Gets security events for a station within a time range.
     */
    public List<SecurityEvent> getSecurityEvents(
            StationId stationId,
            LocalDateTime start,
            LocalDateTime end) {

        return securityEventRepository.findByStationIdAndTimestampBetween(stationId, start, end);
    }

    /**
     * Gets security events by type.
     */
    public List<SecurityEvent> getSecurityEventsByType(String eventType) {
        return securityEventRepository.findByEventType(eventType);
    }

    /**
     * Analyzes security threats based on event patterns.
     * 
     * @param stationId Station to analyze
     * @param hours     Number of hours to look back
     * @return Number of critical events found
     */
    public int analyzeSecurityThreats(StationId stationId, int hours) {
        LocalDateTime start = LocalDateTime.now().minusHours(hours);
        LocalDateTime end = LocalDateTime.now();

        List<SecurityEvent> events = getSecurityEvents(stationId, start, end);

        int criticalCount = (int) events.stream()
                .filter(event -> isCriticalEvent(event.getEventType()))
                .count();

        if (criticalCount > 0) {
            logger.warn("Found {} critical security events for station {} in last {} hours",
                    criticalCount, stationId, hours);
        }

        return criticalCount;
    }

    /**
     * Cleans up old security events.
     * 
     * @param daysToKeep Number of days to keep events
     */
    public void cleanupOldEvents(int daysToKeep) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysToKeep);
        logger.info("Cleaning up security events older than {}", cutoffDate);
        securityEventRepository.deleteOlderThan(cutoffDate);
    }

    /**
     * Determines if an event type is critical.
     */
    private boolean isCriticalEvent(String eventType) {
        return eventType.contains("Failed")
                || eventType.contains("Invalid")
                || eventType.contains("Tamper")
                || eventType.contains("Reset");
    }
}
