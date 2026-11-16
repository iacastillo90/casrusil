package com.casrusil.siierpai.modules.integration_sii.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA entity for the per-company SII synchronization state.
 */
@Entity
@Table(name = "sii_sync_state", schema = "integration_sii")
public class SiiSyncStateEntity {

    @Id
    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "last_sync_at")
    private LocalDateTime lastSyncAt;

    @Column(name = "next_sync_at")
    private LocalDateTime nextSyncAt;

    @Column(name = "sii_status", length = 16)
    private String siiStatus;

    @Column(name = "caf_status", length = 16)
    private String cafStatus;

    @Column(name = "dte_status", length = 16)
    private String dteStatus;

    @Column(name = "last_error", length = 500)
    private String lastError;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public SiiSyncStateEntity() {
    }

    public UUID getCompanyId() {
        return companyId;
    }

    public void setCompanyId(UUID companyId) {
        this.companyId = companyId;
    }

    public LocalDateTime getLastSyncAt() {
        return lastSyncAt;
    }

    public void setLastSyncAt(LocalDateTime lastSyncAt) {
        this.lastSyncAt = lastSyncAt;
    }

    public LocalDateTime getNextSyncAt() {
        return nextSyncAt;
    }

    public void setNextSyncAt(LocalDateTime nextSyncAt) {
        this.nextSyncAt = nextSyncAt;
    }

    public String getSiiStatus() {
        return siiStatus;
    }

    public void setSiiStatus(String siiStatus) {
        this.siiStatus = siiStatus;
    }

    public String getCafStatus() {
        return cafStatus;
    }

    public void setCafStatus(String cafStatus) {
        this.cafStatus = cafStatus;
    }

    public String getDteStatus() {
        return dteStatus;
    }

    public void setDteStatus(String dteStatus) {
        this.dteStatus = dteStatus;
    }

    public String getLastError() {
        return lastError;
    }

    public void setLastError(String lastError) {
        this.lastError = lastError;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
