package com.casrusil.siierpai.modules.integration_sii.infrastructure.adapter.out.persistence.repository;

import com.casrusil.siierpai.modules.integration_sii.infrastructure.adapter.out.persistence.entity.SiiSyncStateEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SiiSyncStateJpaRepository extends JpaRepository<SiiSyncStateEntity, UUID> {
}
