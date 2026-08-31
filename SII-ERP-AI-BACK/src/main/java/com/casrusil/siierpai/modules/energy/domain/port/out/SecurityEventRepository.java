package com.casrusil.siierpai.modules.energy.domain.port.out;

import com.casrusil.siierpai.modules.energy.domain.model.StationId;
import com.casrusil.siierpai.modules.energy.domain.model.security.SecurityEvent;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repository port for SecurityEvent persistence.
 */
public interface SecurityEventRepository {

    /**
     * Saves a security event.
     */
    SecurityEvent save(SecurityEvent event);

    /**
     * Finds all security events for a station.
     */
    List<SecurityEvent> findByStationId(StationId stationId);

    /**
     * Finds security events by type.
     */
    List<SecurityEvent> findByEventType(String eventType);

    /**
     * Finds security events within a time range.
     */
    List<SecurityEvent> findByTimestampBetween(LocalDateTime start, LocalDateTime end);

    /**
     * Finds security events for a station within a time range.
     */
    List<SecurityEvent> findByStationIdAndTimestampBetween(
            StationId stationId,
            LocalDateTime start,
            LocalDateTime end);

    /**
     * Deletes old security events (for cleanup).
     */
    void deleteOlderThan(LocalDateTime timestamp);
}
