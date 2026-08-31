package com.casrusil.siierpai.modules.procurement.domain.model;

import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class PurchaseOrder {
    private UUID id;
    private CompanyId companyId;
    private String supplierRut;
    private String supplierName;
    private LocalDate orderDate;
    private LocalDate expectedDeliveryDate;
    private PurchaseOrderStatus status;
    private BigDecimal totalAmount;
    private List<PurchaseOrderLine> lines;

    public PurchaseOrder(UUID id, CompanyId companyId, String supplierRut, String supplierName,
                         LocalDate orderDate, LocalDate expectedDeliveryDate, PurchaseOrderStatus status,
                         List<PurchaseOrderLine> lines) {
        this.id = id;
        this.companyId = companyId;
        this.supplierRut = supplierRut;
        this.supplierName = supplierName;
        this.orderDate = orderDate;
        this.expectedDeliveryDate = expectedDeliveryDate;
        this.status = status;
        this.lines = lines;
        this.totalAmount = calculateTotal(lines);
    }

    public static PurchaseOrder create(CompanyId companyId, String supplierRut, String supplierName,
                                       LocalDate expectedDeliveryDate, List<PurchaseOrderLine> lines) {
        return new PurchaseOrder(UUID.randomUUID(), companyId, supplierRut, supplierName,
                LocalDate.now(), expectedDeliveryDate, PurchaseOrderStatus.DRAFT, lines);
    }

    private BigDecimal calculateTotal(List<PurchaseOrderLine> lines) {
        if (lines == null) return BigDecimal.ZERO;
        return lines.stream()
                .map(PurchaseOrderLine::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void markAsSent() {
        if (this.status == PurchaseOrderStatus.DRAFT) {
            this.status = PurchaseOrderStatus.SENT;
        }
    }

    public void markAsReceived() {
        if (this.status == PurchaseOrderStatus.SENT || this.status == PurchaseOrderStatus.PARTIALLY_RECEIVED) {
            this.status = PurchaseOrderStatus.RECEIVED;
        }
    }

    public UUID getId() { return id; }
    public CompanyId getCompanyId() { return companyId; }
    public String getSupplierRut() { return supplierRut; }
    public String getSupplierName() { return supplierName; }
    public LocalDate getOrderDate() { return orderDate; }
    public LocalDate getExpectedDeliveryDate() { return expectedDeliveryDate; }
    public PurchaseOrderStatus getStatus() { return status; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public List<PurchaseOrderLine> getLines() { return lines; }
}
