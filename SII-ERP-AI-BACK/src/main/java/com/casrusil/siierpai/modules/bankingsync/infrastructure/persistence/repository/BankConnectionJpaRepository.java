package com.casrusil.siierpai.modules.bankingsync.infrastructure.persistence.repository;

import com.casrusil.siierpai.modules.bankingsync.infrastructure.persistence.entity.BankConnectionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BankConnectionJpaRepository extends JpaRepository<BankConnectionEntity, UUID> {
    List<BankConnectionEntity> findByCompanyId(UUID companyId);
}
