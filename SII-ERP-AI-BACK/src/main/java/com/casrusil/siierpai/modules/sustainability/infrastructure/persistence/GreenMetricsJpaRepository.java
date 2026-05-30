package com.casrusil.siierpai.modules.sustainability.infrastructure.persistence;

import com.casrusil.siierpai.modules.sustainability.infrastructure.persistence.entity.GreenMetricsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * 🌿 JPA Repository para métricas ambientales.
 * 
 * @since 2.0 - Ferrari Engine Phase 2
 */
public interface GreenMetricsJpaRepository extends JpaRepository<GreenMetricsEntity, UUID> {

    List<GreenMetricsEntity> findByCompanyId(UUID companyId);

    @Query("SELECT COALESCE(SUM(g.carbonFootprint), 0) FROM GreenMetricsEntity g WHERE g.companyId = :companyId")
    BigDecimal calculateTotalCarbonFootprint(@Param("companyId") UUID companyId);

    @Query("SELECT COALESCE(SUM(g.greenScore), 0) FROM GreenMetricsEntity g WHERE g.companyId = :companyId")
    BigDecimal calculateTotalGreenScore(@Param("companyId") UUID companyId);

    @Query("SELECT COUNT(DISTINCT g.accountingEntryId) FROM GreenMetricsEntity g " +
            "WHERE g.companyId = :companyId AND g.category = 'SUSTAINABLE_SUPPLY'")
    long countGreenSuppliers(@Param("companyId") UUID companyId);
}
