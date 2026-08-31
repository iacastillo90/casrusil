package com.casrusil.siierpai.modules.bankingsync.infrastructure.persistence.repository;

import com.casrusil.siierpai.modules.bankingsync.infrastructure.persistence.entity.BankTransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BankTransactionJpaRepository extends JpaRepository<BankTransactionEntity, UUID> {
    List<BankTransactionEntity> findByCompanyId(UUID companyId);
}
