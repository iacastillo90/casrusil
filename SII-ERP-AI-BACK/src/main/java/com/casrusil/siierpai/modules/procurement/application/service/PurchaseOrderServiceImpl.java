package com.casrusil.siierpai.modules.procurement.application.service;

import com.casrusil.siierpai.shared.infrastructure.context.TenantGuard;
import com.casrusil.siierpai.modules.procurement.domain.model.PurchaseOrder;
import com.casrusil.siierpai.modules.procurement.domain.model.PurchaseOrderLine;
import com.casrusil.siierpai.modules.procurement.domain.port.in.PurchaseOrderUseCase;
import com.casrusil.siierpai.modules.procurement.domain.port.out.PurchaseOrderRepository;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class PurchaseOrderServiceImpl implements PurchaseOrderUseCase {

    private final PurchaseOrderRepository purchaseOrderRepository;

    public PurchaseOrderServiceImpl(PurchaseOrderRepository purchaseOrderRepository) {
        this.purchaseOrderRepository = purchaseOrderRepository;
    }

    @Override
    @Transactional
    public PurchaseOrder createPurchaseOrder(CompanyId companyId, String supplierRut, String supplierName,
                                             LocalDate expectedDeliveryDate, List<PurchaseOrderLine> lines) {
        PurchaseOrder po = PurchaseOrder.create(companyId, supplierRut, supplierName, expectedDeliveryDate, lines);
        return purchaseOrderRepository.save(po);
    }

    @Override
    public PurchaseOrder getPurchaseOrder(UUID id) {
        return TenantGuard.requireOwned(purchaseOrderRepository.findById(id),
                PurchaseOrder::getCompanyId, "Orden de compra");
    }

    @Override
    public List<PurchaseOrder> getPurchaseOrdersByCompany(CompanyId companyId) {
        return purchaseOrderRepository.findByCompanyId(companyId);
    }

    @Override
    @Transactional
    public PurchaseOrder markAsSent(UUID id) {
        PurchaseOrder po = getPurchaseOrder(id);
        po.markAsSent();
        return purchaseOrderRepository.save(po);
    }

    @Override
    @Transactional
    public PurchaseOrder markAsReceived(UUID id) {
        PurchaseOrder po = getPurchaseOrder(id);
        po.markAsReceived();
        // TODO: Emit Domain Event to notify Inventory to increase stock and Accounts Payable to record debt.
        return purchaseOrderRepository.save(po);
    }
}
