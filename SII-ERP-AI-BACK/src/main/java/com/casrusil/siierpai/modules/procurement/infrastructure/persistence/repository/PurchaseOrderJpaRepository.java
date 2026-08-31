package com.casrusil.siierpai.modules.procurement.infrastructure.persistence.repository;

import com.casrusil.siierpai.modules.procurement.infrastructure.persistence.entity.PurchaseOrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PurchaseOrderJpaRepository extends JpaRepository<PurchaseOrderEntity, UUID> {
    List<PurchaseOrderEntity> findByCompanyId(UUID companyId);
}
