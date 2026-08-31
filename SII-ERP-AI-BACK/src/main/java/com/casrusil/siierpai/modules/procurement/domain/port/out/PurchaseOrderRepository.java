package com.casrusil.siierpai.modules.procurement.domain.port.out;

import com.casrusil.siierpai.modules.procurement.domain.model.PurchaseOrder;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PurchaseOrderRepository {
    PurchaseOrder save(PurchaseOrder purchaseOrder);
    Optional<PurchaseOrder> findById(UUID id);
    List<PurchaseOrder> findByCompanyId(CompanyId companyId);
}
