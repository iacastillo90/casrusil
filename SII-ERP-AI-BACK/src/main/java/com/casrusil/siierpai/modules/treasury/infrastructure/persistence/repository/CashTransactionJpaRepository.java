package com.casrusil.siierpai.modules.treasury.infrastructure.persistence.repository;

import com.casrusil.siierpai.modules.treasury.infrastructure.persistence.entity.CashTransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CashTransactionJpaRepository extends JpaRepository<CashTransactionEntity, UUID> {

    List<CashTransactionEntity> findByCompanyIdOrderByDateDesc(UUID companyId);
}
