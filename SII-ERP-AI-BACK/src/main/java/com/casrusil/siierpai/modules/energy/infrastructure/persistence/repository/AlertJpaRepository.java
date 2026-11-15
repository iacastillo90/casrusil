package com.casrusil.siierpai.modules.energy.infrastructure.persistence.repository;

import com.casrusil.siierpai.modules.energy.infrastructure.persistence.entity.AlertEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AlertJpaRepository extends JpaRepository<AlertEntity, UUID> {
    
    @Query("SELECT a FROM AlertEntity a WHERE a.resolvedAt IS NULL ORDER BY a.createdAt DESC")
    List<AlertEntity> findActive();
    
    @Query("SELECT a FROM AlertEntity a WHERE a.acknowledged = false AND a.resolvedAt IS NULL ORDER BY a.createdAt DESC")
    List<AlertEntity> findUnacknowledged();
    
    @Query("SELECT a FROM AlertEntity a WHERE a.severity = :severity AND a.resolvedAt IS NULL ORDER BY a.createdAt DESC")
    List<AlertEntity> findBySeverity(String severity);
    
    @Query("SELECT a FROM AlertEntity a WHERE a.stationId = :stationId AND a.resolvedAt IS NULL ORDER BY a.createdAt DESC")
    List<AlertEntity> findByStationId(UUID stationId);
    
    @Query("SELECT COUNT(a) FROM AlertEntity a WHERE a.resolvedAt IS NULL")
    long countActive();
    
    @Query("SELECT COUNT(a) FROM AlertEntity a WHERE a.severity = 'CRITICAL' AND a.resolvedAt IS NULL")
    long countCritical();
    
    @Query("SELECT COUNT(a) FROM AlertEntity a WHERE a.acknowledged = false AND a.resolvedAt IS NULL")
    long countUnacknowledged();
}
