package com.casrusil.siierpai.modules.pos.infrastructure.persistence.repository;

import com.casrusil.siierpai.modules.pos.infrastructure.persistence.entity.PosTransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PosTransactionJpaRepository extends JpaRepository<PosTransactionEntity, UUID> {
    List<PosTransactionEntity> findByCompanyId(UUID companyId);
    List<PosTransactionEntity> findByCompanyIdAndStatus(UUID companyId, String status);
}
