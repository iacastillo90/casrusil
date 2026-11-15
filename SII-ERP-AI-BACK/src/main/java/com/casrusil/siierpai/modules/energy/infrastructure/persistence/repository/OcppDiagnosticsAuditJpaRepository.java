package com.casrusil.siierpai.modules.energy.infrastructure.persistence.repository;

import com.casrusil.siierpai.modules.energy.infrastructure.persistence.entity.OcppDiagnosticsAuditEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface OcppDiagnosticsAuditJpaRepository extends JpaRepository<OcppDiagnosticsAuditEntity, UUID> {
}
