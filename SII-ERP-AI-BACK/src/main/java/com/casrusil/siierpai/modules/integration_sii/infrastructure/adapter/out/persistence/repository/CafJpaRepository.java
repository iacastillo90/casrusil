package com.casrusil.siierpai.modules.integration_sii.infrastructure.adapter.out.persistence.repository;
import com.casrusil.siierpai.modules.integration_sii.infrastructure.adapter.out.persistence.entity.CafEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface CafJpaRepository extends JpaRepository<CafEntity, UUID> {
    Optional<CafEntity> findTopByCompanyIdOrderByInitialFolioDesc(String companyId);

    boolean existsByCompanyId(String companyId);
}