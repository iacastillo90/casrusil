package com.casrusil.siierpai.modules.integration_sii.infrastructure.adapter.out.persistence.repository;
import com.casrusil.siierpai.modules.integration_sii.infrastructure.adapter.out.persistence.entity.SiiTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface SiiTokenJpaRepository extends JpaRepository<SiiTokenEntity, UUID> {
    Optional<SiiTokenEntity> findByCompanyId(String companyId);
}