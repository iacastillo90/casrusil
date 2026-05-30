package com.casrusil.siierpai.modules.sustainability.infrastructure.persistence;

import com.casrusil.siierpai.modules.sustainability.domain.model.GreenMetrics;
import com.casrusil.siierpai.modules.sustainability.domain.port.out.GreenMetricsRepository;
import com.casrusil.siierpai.modules.sustainability.infrastructure.persistence.entity.GreenMetricsEntity;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 🌿 Adaptador JPA para persistencia de métricas ambientales.
 * 
 * @since 2.0 - Ferrari Engine Phase 2
 */
@Component
public class GreenMetricsJpaAdapter implements GreenMetricsRepository {

    private final GreenMetricsJpaRepository jpaRepository;

    public GreenMetricsJpaAdapter(GreenMetricsJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void save(GreenMetrics metrics) {
        GreenMetricsEntity entity = toEntity(metrics);
        jpaRepository.save(entity);
    }

    @Override
    public List<GreenMetrics> findByCompanyId(CompanyId companyId) {
        return jpaRepository.findByCompanyId(companyId.value())
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public BigDecimal calculateTotalCarbonFootprint(CompanyId companyId) {
        return jpaRepository.calculateTotalCarbonFootprint(companyId.value());
    }

    @Override
    public BigDecimal calculateTotalGreenScore(CompanyId companyId) {
        return jpaRepository.calculateTotalGreenScore(companyId.value());
    }

    @Override
    public long countGreenSuppliers(CompanyId companyId) {
        return jpaRepository.countGreenSuppliers(companyId.value());
    }

    private GreenMetricsEntity toEntity(GreenMetrics metrics) {
        return new GreenMetricsEntity(
                metrics.getId(),
                metrics.getCompanyId().value(),
                metrics.getAccountingEntryId(),
                metrics.getCategory(),
                metrics.getCarbonFootprint(),
                metrics.getGreenScore(),
                metrics.getEvidence(),
                metrics.getCreatedAt());
    }

    private GreenMetrics toDomain(GreenMetricsEntity entity) {
        return GreenMetrics.create(
                new CompanyId(entity.getCompanyId()),
                entity.getAccountingEntryId(),
                entity.getCategory(),
                entity.getCarbonFootprint(),
                entity.getGreenScore(),
                entity.getEvidence());
    }
}
