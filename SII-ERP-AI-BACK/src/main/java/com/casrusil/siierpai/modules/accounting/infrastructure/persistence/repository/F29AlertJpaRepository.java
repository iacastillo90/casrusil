package com.casrusil.siierpai.modules.accounting.infrastructure.persistence.repository;

import com.casrusil.siierpai.modules.accounting.infrastructure.persistence.entity.F29AlertEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface F29AlertJpaRepository extends JpaRepository<F29AlertEntity, UUID> {

    List<F29AlertEntity> findByCompanyIdOrderByCreatedAtDesc(UUID companyId);

    boolean existsByCompanyIdAndPeriodYearAndPeriodMonthAndInvoiceFolio(
            UUID companyId, Integer periodYear, Integer periodMonth, Long invoiceFolio);
}
