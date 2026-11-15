package com.casrusil.siierpai.modules.banking.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Entidad JPA para transacciones de conciliación bancaria manual.
 * 
 * <p>
 * Representa un movimiento en la cuenta bancaria de la empresa para la
 * conciliación manual. Se utiliza para la conciliación bancaria manual
 * y su reconciliación con asientos contables.
 * 
 * <p>
 * Puede estar conciliada con un asiento contable existente.
 * 
 * @since 2.0 - Ferrari Engine Refactoring
 */
@Entity(name = "ReconciliationTransactionEntity")
@Table(name = "bank_reconciliation_transactions", indexes = { 
    @Index(name = "idx_recon_tx_company", columnList = "company_id"),
    @Index(name = "idx_recon_tx_date", columnList = "date")
})
public class ReconciliationTransactionEntity {

    @Id
    private UUID id;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(length = 100)
    private String reference;

    @Column(nullable = false)
    private boolean reconciled = false;

    @Column(name = "reconciled_with_entry_id")
    private UUID reconciledWithEntryId;

    // Constructors
    public ReconciliationTransactionEntity() {
    }

    public ReconciliationTransactionEntity(UUID id, UUID companyId, LocalDate date, String description,
            BigDecimal amount, String reference, boolean reconciled, UUID reconciledWithEntryId) {
        this.id = id;
        this.companyId = companyId;
        this.date = date;
        this.description = description;
        this.amount = amount;
        this.reference = reference;
        this.reconciled = reconciled;
        this.reconciledWithEntryId = reconciledWithEntryId;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getCompanyId() {
        return companyId;
    }

    public void setCompanyId(UUID companyId) {
        this.companyId = companyId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public boolean isReconciled() {
        return reconciled;
    }

    public void setReconciled(boolean reconciled) {
        this.reconciled = reconciled;
    }

    public UUID getReconciledWithEntryId() {
        return reconciledWithEntryId;
    }

    public void setReconciledWithEntryId(UUID reconciledWithEntryId) {
        this.reconciledWithEntryId = reconciledWithEntryId;
    }
}