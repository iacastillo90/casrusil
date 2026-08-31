package com.casrusil.siierpai.modules.dms.infrastructure.persistence.repository;
import com.casrusil.siierpai.modules.dms.infrastructure.persistence.entity.CorporateDocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;
public interface CorporateDocumentJpaRepository extends JpaRepository<CorporateDocumentEntity, UUID> {
    List<CorporateDocumentEntity> findByCompanyId(String companyId);
}