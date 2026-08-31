package com.casrusil.siierpai.modules.procurement.domain.port.in;

import com.casrusil.siierpai.modules.procurement.domain.model.PurchaseOrder;
import com.casrusil.siierpai.modules.procurement.domain.model.PurchaseOrderLine;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface PurchaseOrderUseCase {
    PurchaseOrder createPurchaseOrder(CompanyId companyId, String supplierRut, String supplierName,
                                      LocalDate expectedDeliveryDate, List<PurchaseOrderLine> lines);
    PurchaseOrder getPurchaseOrder(UUID id);
    List<PurchaseOrder> getPurchaseOrdersByCompany(CompanyId companyId);
    PurchaseOrder markAsSent(UUID id);
    PurchaseOrder markAsReceived(UUID id);
}
