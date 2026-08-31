package com.casrusil.siierpai.modules.fixed_assets.infrastructure.persistence.repository;

import com.casrusil.siierpai.modules.fixed_assets.infrastructure.persistence.entity.FixedAssetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface FixedAssetJpaRepository extends JpaRepository<FixedAssetEntity, UUID> {
    List<FixedAssetEntity> findByCompanyId(String companyId);
}