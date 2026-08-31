package com.casrusil.siierpai.modules.procurement.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "purchase_orders")
public class PurchaseOrderEntity {

    @Id
    private UUID id;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "supplier_rut", nullable = false)
    private String supplierRut;

    @Column(name = "supplier_name", nullable = false)
    private String supplierName;

    @Column(name = "order_date", nullable = false)
    private LocalDate orderDate;

    @Column(name = "expected_delivery_date")
    private LocalDate expectedDeliveryDate;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_order_id")
    private List<PurchaseOrderLineEntity> lines;

    protected PurchaseOrderEntity() {}

    public PurchaseOrderEntity(UUID id, UUID companyId, String supplierRut, String supplierName,
                               LocalDate orderDate, LocalDate expectedDeliveryDate, String status,
                               BigDecimal totalAmount, List<PurchaseOrderLineEntity> lines) {
        this.id = id;
        this.companyId = companyId;
        this.supplierRut = supplierRut;
        this.supplierName = supplierName;
        this.orderDate = orderDate;
        this.expectedDeliveryDate = expectedDeliveryDate;
        this.status = status;
        this.totalAmount = totalAmount;
        this.lines = lines;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getCompanyId() { return companyId; }
    public void setCompanyId(UUID companyId) { this.companyId = companyId; }
    public String getSupplierRut() { return supplierRut; }
    public void setSupplierRut(String supplierRut) { this.supplierRut = supplierRut; }
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    public LocalDate getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDate orderDate) { this.orderDate = orderDate; }
    public LocalDate getExpectedDeliveryDate() { return expectedDeliveryDate; }
    public void setExpectedDeliveryDate(LocalDate expectedDeliveryDate) { this.expectedDeliveryDate = expectedDeliveryDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public List<PurchaseOrderLineEntity> getLines() { return lines; }
    public void setLines(List<PurchaseOrderLineEntity> lines) { this.lines = lines; }
}
