package com.casrusil.siierpai.modules.invoicing.application.service;

import com.casrusil.siierpai.modules.invoicing.domain.model.InvoiceAuditLog;
import com.casrusil.siierpai.modules.invoicing.domain.port.in.GetInvoiceAuditTrailUseCase;
import com.casrusil.siierpai.modules.invoicing.domain.port.out.InvoiceAuditLogRepository;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import com.casrusil.siierpai.shared.infrastructure.context.CompanyContext;
import com.casrusil.siierpai.shared.infrastructure.context.UserContext;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class InvoiceAuditTrailService implements GetInvoiceAuditTrailUseCase {

    private final InvoiceAuditLogRepository auditLogRepository;

    public InvoiceAuditTrailService(InvoiceAuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void record(CompanyId companyId, UUID invoiceId, String action, String detail) {
        auditLogRepository.save(InvoiceAuditLog.record(
                companyId,
                invoiceId,
                action,
                UserContext.getUserName(),
                detail));
    }

    @Override
    public List<InvoiceAuditLog> getAuditTrail(UUID invoiceId) {
        CompanyId companyId = CompanyContext.requireCompanyId();
        return auditLogRepository.findByCompanyIdAndInvoiceIdOrderByOccurredAtDesc(companyId, invoiceId);
    }
}
