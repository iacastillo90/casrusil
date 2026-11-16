package com.casrusil.siierpai.modules.invoicing.domain.port.in;

import com.casrusil.siierpai.modules.invoicing.domain.model.InvoiceAuditLog;

import java.util.List;
import java.util.UUID;

public interface GetInvoiceAuditTrailUseCase {

    List<InvoiceAuditLog> getAuditTrail(UUID invoiceId);
}
