package com.casrusil.siierpai.modules.mrp.domain.model;

import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class WorkOrder {
    private final UUID id;
    private final CompanyId companyId;
    private final UUID bomId;
    private final BigDecimal targetQuantity;
    private String status; // PENDING, IN_PROGRESS, COMPLETED
    private final LocalDate createdAt;

    public WorkOrder(UUID id, CompanyId companyId, UUID bomId, BigDecimal targetQuantity, String status, LocalDate createdAt) {
        this.id = id;
        this.companyId = companyId;
        this.bomId = bomId;
        this.targetQuantity = targetQuantity;
        this.status = status;
        this.createdAt = createdAt;
    }

    public static WorkOrder create(CompanyId companyId, UUID bomId, BigDecimal targetQuantity) {
        return new WorkOrder(UUID.randomUUID(), companyId, bomId, targetQuantity, "PENDING", LocalDate.now());
    }

    public void complete() {
        this.status = "COMPLETED";
    }

    public UUID getId() { return id; }
    public CompanyId getCompanyId() { return companyId; }
    public UUID getBomId() { return bomId; }
    public BigDecimal getTargetQuantity() { return targetQuantity; }
    public String getStatus() { return status; }
    public LocalDate getCreatedAt() { return createdAt; }
}