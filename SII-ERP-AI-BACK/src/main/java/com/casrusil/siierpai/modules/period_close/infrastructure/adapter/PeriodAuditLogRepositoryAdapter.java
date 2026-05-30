package com.casrusil.siierpai.modules.period_close.infrastructure.adapter;

import com.casrusil.siierpai.modules.period_close.domain.model.PeriodAuditLog;
import com.casrusil.siierpai.modules.period_close.domain.port.PeriodAuditLogRepositoryPort;
import com.casrusil.siierpai.modules.period_close.infrastructure.persistence.entity.PeriodAuditLogEntity;
import com.casrusil.siierpai.modules.period_close.infrastructure.persistence.repository.PeriodAuditLogJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PeriodAuditLogRepositoryAdapter implements PeriodAuditLogRepositoryPort {

    private final PeriodAuditLogJpaRepository jpaRepository;

    @Override
    public PeriodAuditLog save(PeriodAuditLog log) {
        PeriodAuditLogEntity entity = mapToEntity(log);
        return mapToDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<PeriodAuditLog> findByPeriodAndCompanyId(YearMonth period, String companyId) {
        return jpaRepository.findByPeriodAndCompanyId(period, companyId).map(this::mapToDomain);
    }
    
    private PeriodAuditLogEntity mapToEntity(PeriodAuditLog domain) {
        PeriodAuditLogEntity entity = new PeriodAuditLogEntity();
        entity.setId(domain.getId());
        entity.setCompanyId(domain.getCompanyId());
        entity.setPeriod(domain.getPeriod());
        entity.setAuditDate(domain.getAuditDate());
        entity.setIssuesFound(domain.getIssuesFound());
        entity.setResolved(domain.isResolved());
        return entity;
    }
    
    private PeriodAuditLog mapToDomain(PeriodAuditLogEntity entity) {
        return PeriodAuditLog.builder()
                .id(entity.getId())
                .companyId(entity.getCompanyId())
                .period(entity.getPeriod())
                .auditDate(entity.getAuditDate())
                .issuesFound(entity.getIssuesFound())
                .isResolved(entity.isResolved())
                .build();
    }
}
