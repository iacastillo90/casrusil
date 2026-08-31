package com.casrusil.siierpai.modules.banking.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface ReconciliationLearningJpaRepository extends JpaRepository<ReconciliationLearningEntity, UUID> {
    List<ReconciliationLearningEntity> findByCompanyId(String companyId);
}
