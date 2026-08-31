package com.casrusil.siierpai.modules.wms.application.service;

import com.casrusil.siierpai.modules.wms.domain.model.PickingTask;
import com.casrusil.siierpai.modules.wms.domain.model.WarehouseLocation;
import com.casrusil.siierpai.modules.wms.infrastructure.persistence.entity.PickingItemEntity;
import com.casrusil.siierpai.modules.wms.infrastructure.persistence.entity.PickingTaskEntity;
import com.casrusil.siierpai.modules.wms.infrastructure.persistence.entity.WarehouseLocationEntity;
import com.casrusil.siierpai.modules.wms.infrastructure.persistence.repository.PickingTaskJpaRepository;
import com.casrusil.siierpai.modules.wms.infrastructure.persistence.repository.WarehouseLocationJpaRepository;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class WmsServiceImpl {
    private final WarehouseLocationJpaRepository locRepo;
    private final PickingTaskJpaRepository taskRepo;

    public WmsServiceImpl(WarehouseLocationJpaRepository locRepo, PickingTaskJpaRepository taskRepo) {
        this.locRepo = locRepo;
        this.taskRepo = taskRepo;
    }

    public WarehouseLocation createLocation(CompanyId companyId, String code, String description, String zone) {
        WarehouseLocationEntity e = new WarehouseLocationEntity();
        e.setId(UUID.randomUUID());
        e.setCompanyId(companyId.value().toString());
        e.setCode(code);
        e.setDescription(description);
        e.setZone(zone);
        locRepo.save(e);
        return new WarehouseLocation(e.getId(), companyId, code, description, zone);
    }

    public List<WarehouseLocation> getLocations(CompanyId companyId) {
        return locRepo.findByCompanyId(companyId.value().toString()).stream()
            .map(e -> new WarehouseLocation(e.getId(), companyId, e.getCode(), e.getDescription(), e.getZone()))
            .collect(Collectors.toList());
    }

    public PickingTask createPickingTask(CompanyId companyId, String orderId, List<PickingTask.PickingItem> items) {
        PickingTaskEntity e = new PickingTaskEntity();
        e.setId(UUID.randomUUID());
        e.setCompanyId(companyId.value().toString());
        e.setOrderId(orderId);
        e.setStatus("PENDING");
        
        List<PickingItemEntity> ents = items.stream().map(i -> {
            PickingItemEntity ie = new PickingItemEntity();
            ie.setProductId(i.productId());
            ie.setQuantity(i.quantity());
            ie.setLocationCode(i.locationCode());
            return ie;
        }).collect(Collectors.toList());
        e.setItems(ents);
        
        taskRepo.save(e);
        return new PickingTask(e.getId(), companyId, orderId, items, "PENDING");
    }

    public List<PickingTask> getPickingTasks(CompanyId companyId) {
        return taskRepo.findByCompanyId(companyId.value().toString()).stream().map(e -> {
            List<PickingTask.PickingItem> items = e.getItems().stream()
                .map(i -> new PickingTask.PickingItem(i.getProductId(), i.getQuantity(), i.getLocationCode()))
                .collect(Collectors.toList());
            return new PickingTask(e.getId(), companyId, e.getOrderId(), items, e.getStatus());
        }).collect(Collectors.toList());
    }

    public void completePickingTask(CompanyId companyId, UUID taskId) {
        taskRepo.findById(taskId)
            .filter(t -> t.getCompanyId().equals(companyId.value().toString()))
            .ifPresent(t -> {
                t.setStatus("COMPLETED");
                taskRepo.save(t);
            });
    }
}