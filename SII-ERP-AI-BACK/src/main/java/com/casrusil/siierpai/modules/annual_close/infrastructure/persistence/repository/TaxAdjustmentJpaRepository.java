package com.casrusil.siierpai.modules.annual_close.infrastructure.persistence.repository;

import com.casrusil.siierpai.modules.annual_close.infrastructure.persistence.entity.TaxAdjustmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaxAdjustmentJpaRepository extends JpaRepository<TaxAdjustmentEntity, Long> {
    List<TaxAdjustmentEntity> findByCompanyIdAndFiscalYear(String companyId, int fiscalYear);
}
