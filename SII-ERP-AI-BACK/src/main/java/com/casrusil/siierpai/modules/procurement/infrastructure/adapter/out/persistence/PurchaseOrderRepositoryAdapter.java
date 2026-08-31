package com.casrusil.siierpai.modules.procurement.infrastructure.adapter.out.persistence;

import com.casrusil.siierpai.modules.procurement.domain.model.PurchaseOrder;
import com.casrusil.siierpai.modules.procurement.domain.model.PurchaseOrderLine;
import com.casrusil.siierpai.modules.procurement.domain.model.PurchaseOrderStatus;
import com.casrusil.siierpai.modules.procurement.domain.port.out.PurchaseOrderRepository;
import com.casrusil.siierpai.modules.procurement.infrastructure.persistence.entity.PurchaseOrderEntity;
import com.casrusil.siierpai.modules.procurement.infrastructure.persistence.entity.PurchaseOrderLineEntity;
import com.casrusil.siierpai.modules.procurement.infrastructure.persistence.repository.PurchaseOrderJpaRepository;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class PurchaseOrderRepositoryAdapter implements PurchaseOrderRepository {

    private final PurchaseOrderJpaRepository jpaRepository;

    public PurchaseOrderRepositoryAdapter(PurchaseOrderJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public PurchaseOrder save(PurchaseOrder purchaseOrder) {
        PurchaseOrderEntity entity = toEntity(purchaseOrder);
        PurchaseOrderEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<PurchaseOrder> findById(UUID id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<PurchaseOrder> findByCompanyId(CompanyId companyId) {
        return jpaRepository.findByCompanyId(companyId.value()).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private PurchaseOrderEntity toEntity(PurchaseOrder domain) {
        List<PurchaseOrderLineEntity> lineEntities = domain.getLines().stream()
                .map(l -> new PurchaseOrderLineEntity(
                        l.getId(), l.getProductId(), l.getDescription(),
                        l.getQuantity(), l.getUnitPrice(), l.getTotalAmount()
                )).collect(Collectors.toList());

        return new PurchaseOrderEntity(
                domain.getId(),
                domain.getCompanyId().value(),
                domain.getSupplierRut(),
                domain.getSupplierName(),
                domain.getOrderDate(),
                domain.getExpectedDeliveryDate(),
                domain.getStatus().name(),
                domain.getTotalAmount(),
                lineEntities
        );
    }

    private PurchaseOrder toDomain(PurchaseOrderEntity entity) {
        List<PurchaseOrderLine> lines = entity.getLines().stream()
                .map(l -> new PurchaseOrderLine(
                        l.getId(), l.getProductId(), l.getDescription(),
                        l.getQuantity(), l.getUnitPrice()
                )).collect(Collectors.toList());

        return new PurchaseOrder(
                entity.getId(),
                CompanyId.of(entity.getCompanyId()),
                entity.getSupplierRut(),
                entity.getSupplierName(),
                entity.getOrderDate(),
                entity.getExpectedDeliveryDate(),
                PurchaseOrderStatus.valueOf(entity.getStatus()),
                lines
        );
    }
}
