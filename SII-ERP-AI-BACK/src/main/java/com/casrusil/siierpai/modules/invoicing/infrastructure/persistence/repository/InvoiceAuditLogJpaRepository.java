package com.casrusil.siierpai.modules.invoicing.infrastructure.persistence.repository;

import com.casrusil.siierpai.modules.invoicing.infrastructure.persistence.entity.InvoiceAuditLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface InvoiceAuditLogJpaRepository extends JpaRepository<InvoiceAuditLogEntity, UUID> {

    List<InvoiceAuditLogEntity> findByCompanyIdAndInvoiceIdOrderByOccurredAtDesc(UUID companyId, UUID invoiceId);
}
