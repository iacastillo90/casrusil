package com.casrusil.siierpai.modules.energy.infrastructure.persistence.repository;

import com.casrusil.siierpai.modules.energy.infrastructure.persistence.entity.SecurityEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA Repository for SecurityEventEntity.
 * 
 * <p>
 * Provides database access for security event persistence with
 * custom queries for time-range filtering and cleanup operations.
 * 
 * @since 1.0
 */
@Repository
public interface SecurityEventJpaRepository extends JpaRepository<SecurityEventEntity, UUID> {

    /**
     * Find all security events for a specific station.
     */
    @Query("SELECT s FROM SecurityEventEntity s WHERE s.stationId = :stationId ORDER BY s.timestamp DESC")
    List<SecurityEventEntity> findByStationId(@Param("stationId") UUID stationId);

    /**
     * Find security events by type.
     */
    @Query("SELECT s FROM SecurityEventEntity s WHERE s.eventType = :eventType ORDER BY s.timestamp DESC")
    List<SecurityEventEntity> findByEventType(@Param("eventType") String eventType);

    /**
     * Find security events within a time range.
     */
    @Query("SELECT s FROM SecurityEventEntity s WHERE s.timestamp BETWEEN :start AND :end ORDER BY s.timestamp DESC")
    List<SecurityEventEntity> findByTimestampBetween(@Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    /**
     * Find security events for a station within a time range.
     */
    @Query("SELECT s FROM SecurityEventEntity s WHERE s.stationId = :stationId AND s.timestamp BETWEEN :start AND :end ORDER BY s.timestamp DESC")
    List<SecurityEventEntity> findByStationIdAndTimestampBetween(
            @Param("stationId") UUID stationId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    /**
     * Delete old security events (cleanup operation).
     */
    @Modifying
    @Query("DELETE FROM SecurityEventEntity s WHERE s.timestamp < :timestamp")
    void deleteOlderThan(@Param("timestamp") LocalDateTime timestamp);
}
