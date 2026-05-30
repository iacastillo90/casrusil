package com.casrusil.siierpai.modules.period_close.infrastructure.persistence.repository;

import com.casrusil.siierpai.modules.period_close.infrastructure.persistence.entity.PeriodAuditLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.YearMonth;
import java.util.Optional;

@Repository
public interface PeriodAuditLogJpaRepository extends JpaRepository<PeriodAuditLogEntity, Long> {
    Optional<PeriodAuditLogEntity> findByPeriodAndCompanyId(YearMonth period, String companyId);
}
