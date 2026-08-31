package com.casrusil.siierpai.modules.wms.domain.model;

import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import java.util.UUID;
import java.util.List;

public class PickingTask {
    private final UUID id;
    private final CompanyId companyId;
    private final String orderId;
    private final List<PickingItem> items;
    private String status; // PENDING, IN_PROGRESS, COMPLETED

    public PickingTask(UUID id, CompanyId companyId, String orderId, List<PickingItem> items, String status) {
        this.id = id;
        this.companyId = companyId;
        this.orderId = orderId;
        this.items = items;
        this.status = status;
    }

    public static PickingTask create(CompanyId companyId, String orderId, List<PickingItem> items) {
        return new PickingTask(UUID.randomUUID(), companyId, orderId, items, "PENDING");
    }

    public void start() { this.status = "IN_PROGRESS"; }
    public void complete() { this.status = "COMPLETED"; }

    public UUID getId() { return id; }
    public CompanyId getCompanyId() { return companyId; }
    public String getOrderId() { return orderId; }
    public List<PickingItem> getItems() { return items; }
    public String getStatus() { return status; }

    public record PickingItem(String productId, int quantity, String locationCode) {}
}