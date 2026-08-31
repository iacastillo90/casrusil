package com.casrusil.siierpai.modules.mrp.application.service;

import com.casrusil.siierpai.modules.mrp.domain.model.BillOfMaterials;
import com.casrusil.siierpai.modules.mrp.domain.model.WorkOrder;
import com.casrusil.siierpai.modules.mrp.infrastructure.persistence.entity.BomEntity;
import com.casrusil.siierpai.modules.mrp.infrastructure.persistence.entity.BomItemEntity;
import com.casrusil.siierpai.modules.mrp.infrastructure.persistence.entity.WorkOrderEntity;
import com.casrusil.siierpai.modules.mrp.infrastructure.persistence.repository.BomJpaRepository;
import com.casrusil.siierpai.modules.mrp.infrastructure.persistence.repository.WorkOrderJpaRepository;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.time.LocalDate;

@Service
@Transactional
public class MrpServiceImpl {
    
    private final BomJpaRepository bomRepo;
    private final WorkOrderJpaRepository workOrderRepo;

    public MrpServiceImpl(BomJpaRepository bomRepo, WorkOrderJpaRepository workOrderRepo) {
        this.bomRepo = bomRepo;
        this.workOrderRepo = workOrderRepo;
    }

    public BillOfMaterials createBom(CompanyId companyId, String productId, String name, List<BillOfMaterials.BomItem> items) {
        BomEntity entity = new BomEntity();
        entity.setId(UUID.randomUUID());
        entity.setCompanyId(companyId.value().toString());
        entity.setProductId(productId);
        entity.setName(name);
        
        List<BomItemEntity> itemEntities = items.stream().map(i -> {
            BomItemEntity ie = new BomItemEntity();
            ie.setComponentProductId(i.componentProductId());
            ie.setQuantity(i.quantity());
            return ie;
        }).collect(Collectors.toList());
        entity.setItems(itemEntities);
        
        bomRepo.save(entity);
        return new BillOfMaterials(entity.getId(), companyId, productId, name, items);
    }

    public List<BillOfMaterials> getBoms(CompanyId companyId) {
        return bomRepo.findByCompanyId(companyId.value().toString()).stream().map(e -> {
            List<BillOfMaterials.BomItem> items = e.getItems().stream()
                .map(i -> new BillOfMaterials.BomItem(i.getComponentProductId(), i.getQuantity()))
                .collect(Collectors.toList());
            return new BillOfMaterials(e.getId(), companyId, e.getProductId(), e.getName(), items);
        }).collect(Collectors.toList());
    }

    public WorkOrder createWorkOrder(CompanyId companyId, UUID bomId, BigDecimal targetQuantity) {
        WorkOrderEntity entity = new WorkOrderEntity();
        entity.setId(UUID.randomUUID());
        entity.setCompanyId(companyId.value().toString());
        entity.setBomId(bomId);
        entity.setTargetQuantity(targetQuantity);
        entity.setStatus("PENDING");
        entity.setCreatedAt(LocalDate.now());
        workOrderRepo.save(entity);
        return new WorkOrder(entity.getId(), companyId, bomId, targetQuantity, "PENDING", entity.getCreatedAt());
    }

    public List<WorkOrder> getWorkOrders(CompanyId companyId) {
        return workOrderRepo.findByCompanyId(companyId.value().toString()).stream()
            .map(e -> new WorkOrder(e.getId(), companyId, e.getBomId(), e.getTargetQuantity(), e.getStatus(), e.getCreatedAt()))
            .collect(Collectors.toList());
    }

    public void completeWorkOrder(CompanyId companyId, UUID orderId) {
        workOrderRepo.findById(orderId)
            .filter(o -> o.getCompanyId().equals(companyId.value().toString()))
            .ifPresent(o -> {
                o.setStatus("COMPLETED");
                workOrderRepo.save(o);
            });
    }
}