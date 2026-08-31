package com.casrusil.siierpai.modules.pos.domain.model;

import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class PosTransaction {
    private UUID id;
    private CompanyId companyId;
    private String posTerminalId; // ID de la caja registradora
    private String cashierName;
    private LocalDateTime transactionDate;
    private BigDecimal totalAmount;
    private String paymentMethod; // CASH, CARD, TRANSFER
    private String status; // COMPLETED, VOIDED, PENDING_SYNC
    private List<PosTransactionItem> items;

    public PosTransaction(UUID id, CompanyId companyId, String posTerminalId, String cashierName, LocalDateTime transactionDate, String paymentMethod, String status, List<PosTransactionItem> items) {
        this.id = id;
        this.companyId = companyId;
        this.posTerminalId = posTerminalId;
        this.cashierName = cashierName;
        this.transactionDate = transactionDate;
        this.paymentMethod = paymentMethod;
        this.status = status;
        this.items = items;
        this.totalAmount = calculateTotal(items);
    }

    public static PosTransaction create(CompanyId companyId, String posTerminalId, String cashierName, String paymentMethod, List<PosTransactionItem> items) {
        return new PosTransaction(UUID.randomUUID(), companyId, posTerminalId, cashierName, LocalDateTime.now(), paymentMethod, "PENDING_SYNC", items);
    }

    private BigDecimal calculateTotal(List<PosTransactionItem> items) {
        if (items == null) return BigDecimal.ZERO;
        return items.stream()
                .map(PosTransactionItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void markAsCompleted() {
        this.status = "COMPLETED";
    }

    public void voidTransaction() {
        this.status = "VOIDED";
    }

    public UUID getId() { return id; }
    public CompanyId getCompanyId() { return companyId; }
    public String getPosTerminalId() { return posTerminalId; }
    public String getCashierName() { return cashierName; }
    public LocalDateTime getTransactionDate() { return transactionDate; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getStatus() { return status; }
    public List<PosTransactionItem> getItems() { return items; }
}
