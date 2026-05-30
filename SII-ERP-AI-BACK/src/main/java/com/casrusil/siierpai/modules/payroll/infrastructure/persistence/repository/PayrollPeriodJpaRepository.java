package com.casrusil.siierpai.modules.payroll.infrastructure.persistence.repository;

import com.casrusil.siierpai.modules.payroll.infrastructure.persistence.entity.PayrollPeriodEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.YearMonth;
import java.util.Optional;

@Repository
public interface PayrollPeriodJpaRepository extends JpaRepository<PayrollPeriodEntity, Long> {
    Optional<PayrollPeriodEntity> findByPeriodAndCompanyId(YearMonth period, String companyId);
    Optional<PayrollPeriodEntity> findByIdAndCompanyId(Long id, String companyId);
}
