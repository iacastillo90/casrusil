package com.casrusil.siierpai.modules.energy.infrastructure.persistence.repository;

import com.casrusil.siierpai.modules.energy.infrastructure.persistence.entity.CertificateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA Repository for CertificateEntity.
 * 
 * <p>
 * Provides database access for certificate persistence with
 * custom queries for station filtering and validity checks.
 * 
 * @since 1.0
 */
@Repository
public interface CertificateJpaRepository extends JpaRepository<CertificateEntity, UUID> {

    /**
     * Find all certificates for a specific station.
     */
    @Query("SELECT c FROM CertificateEntity c WHERE c.stationId = :stationId ORDER BY c.createdAt DESC")
    List<CertificateEntity> findByStationId(@Param("stationId") UUID stationId);

    /**
     * Find certificates by type.
     */
    @Query("SELECT c FROM CertificateEntity c WHERE c.type = :type ORDER BY c.createdAt DESC")
    List<CertificateEntity> findByType(@Param("type") String type);

    /**
     * Find certificates by station and type.
     */
    @Query("SELECT c FROM CertificateEntity c WHERE c.stationId = :stationId AND c.type = :type ORDER BY c.createdAt DESC")
    List<CertificateEntity> findByStationIdAndType(@Param("stationId") UUID stationId, @Param("type") String type);

    /**
     * Find valid (non-expired) certificates for a station.
     */
    @Query("SELECT c FROM CertificateEntity c WHERE c.stationId = :stationId AND c.validTo > :now AND c.status = 'ACCEPTED' ORDER BY c.createdAt DESC")
    List<CertificateEntity> findValidByStationId(@Param("stationId") UUID stationId, @Param("now") LocalDateTime now);

    /**
     * Delete all certificates for a station.
     */
    @Modifying
    @Query("DELETE FROM CertificateEntity c WHERE c.stationId = :stationId")
    void deleteByStationId(@Param("stationId") UUID stationId);
}
