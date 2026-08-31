package com.casrusil.siierpai.modules.mrp.infrastructure.persistence.repository;

import com.casrusil.siierpai.modules.mrp.infrastructure.persistence.entity.WorkOrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface WorkOrderJpaRepository extends JpaRepository<WorkOrderEntity, UUID> {
    List<WorkOrderEntity> findByCompanyId(String companyId);
}