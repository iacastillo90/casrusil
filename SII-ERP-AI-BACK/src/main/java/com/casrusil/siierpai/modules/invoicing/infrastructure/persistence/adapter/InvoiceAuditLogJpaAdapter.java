package com.casrusil.siierpai.modules.invoicing.infrastructure.persistence.adapter;

import com.casrusil.siierpai.modules.invoicing.domain.model.InvoiceAuditLog;
import com.casrusil.siierpai.modules.invoicing.domain.port.out.InvoiceAuditLogRepository;
import com.casrusil.siierpai.modules.invoicing.infrastructure.persistence.entity.InvoiceAuditLogEntity;
import com.casrusil.siierpai.modules.invoicing.infrastructure.persistence.repository.InvoiceAuditLogJpaRepository;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class InvoiceAuditLogJpaAdapter implements InvoiceAuditLogRepository {

    private final InvoiceAuditLogJpaRepository jpaRepository;

    public InvoiceAuditLogJpaAdapter(InvoiceAuditLogJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void save(InvoiceAuditLog auditLog) {
        jpaRepository.save(toEntity(auditLog));
    }

    @Override
    public List<InvoiceAuditLog> findByCompanyIdAndInvoiceIdOrderByOccurredAtDesc(CompanyId companyId,
            UUID invoiceId) {
        return jpaRepository.findByCompanyIdAndInvoiceIdOrderByOccurredAtDesc(companyId.value(), invoiceId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private InvoiceAuditLogEntity toEntity(InvoiceAuditLog auditLog) {
        return new InvoiceAuditLogEntity(
                auditLog.id(),
                auditLog.invoiceId(),
                auditLog.companyId().value(),
                auditLog.action(),
                auditLog.username(),
                auditLog.detail(),
                auditLog.occurredAt());
    }

    private InvoiceAuditLog toDomain(InvoiceAuditLogEntity entity) {
        return new InvoiceAuditLog(
                entity.getId(),
                entity.getInvoiceId(),
                new CompanyId(entity.getCompanyId()),
                entity.getAction(),
                entity.getUsername(),
                entity.getDetail(),
                entity.getOccurredAt());
    }
}
