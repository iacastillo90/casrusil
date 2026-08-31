package com.casrusil.siierpai.modules.crm.infrastructure.persistence.repository;

import com.casrusil.siierpai.modules.crm.infrastructure.persistence.entity.LeadEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LeadJpaRepository extends JpaRepository<LeadEntity, UUID> {
    List<LeadEntity> findByCompanyId(UUID companyId);
}
