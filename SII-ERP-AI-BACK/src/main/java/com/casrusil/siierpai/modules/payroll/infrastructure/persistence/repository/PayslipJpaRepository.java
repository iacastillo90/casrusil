package com.casrusil.siierpai.modules.payroll.infrastructure.persistence.repository;

import com.casrusil.siierpai.modules.payroll.infrastructure.persistence.entity.PayslipEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PayslipJpaRepository extends JpaRepository<PayslipEntity, Long> {
    List<PayslipEntity> findByPeriodIdAndCompanyId(Long periodId, String companyId);
}
