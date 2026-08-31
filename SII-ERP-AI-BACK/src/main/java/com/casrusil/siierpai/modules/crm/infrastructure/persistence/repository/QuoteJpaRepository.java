package com.casrusil.siierpai.modules.crm.infrastructure.persistence.repository;

import com.casrusil.siierpai.modules.crm.infrastructure.persistence.entity.QuoteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface QuoteJpaRepository extends JpaRepository<QuoteEntity, UUID> {
    List<QuoteEntity> findByCompanyId(UUID companyId);
}
