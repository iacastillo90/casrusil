package com.casrusil.siierpai.modules.mrp.infrastructure.persistence.repository;

import com.casrusil.siierpai.modules.mrp.infrastructure.persistence.entity.BomEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface BomJpaRepository extends JpaRepository<BomEntity, UUID> {
    List<BomEntity> findByCompanyId(String companyId);
}