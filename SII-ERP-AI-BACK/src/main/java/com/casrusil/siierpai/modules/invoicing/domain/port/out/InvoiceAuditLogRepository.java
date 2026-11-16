package com.casrusil.siierpai.modules.invoicing.domain.port.out;

import com.casrusil.siierpai.modules.invoicing.domain.model.InvoiceAuditLog;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;

import java.util.List;
import java.util.UUID;

public interface InvoiceAuditLogRepository {

    void save(InvoiceAuditLog auditLog);

    List<InvoiceAuditLog> findByCompanyIdAndInvoiceIdOrderByOccurredAtDesc(CompanyId companyId, UUID invoiceId);
}
