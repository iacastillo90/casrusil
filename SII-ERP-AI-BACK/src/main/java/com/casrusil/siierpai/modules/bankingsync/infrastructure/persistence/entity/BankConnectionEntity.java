package com.casrusil.siierpai.modules.bankingsync.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "bank_connections")
public class BankConnectionEntity {

    @Id
    private UUID id;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "bank_name", nullable = false)
    private String bankName;

    @Column(name = "account_number", nullable = false)
    private String accountNumber;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "last_sync")
    private String lastSync;

    protected BankConnectionEntity() {}

    public BankConnectionEntity(UUID id, UUID companyId, String bankName, String accountNumber, String status, String lastSync) {
        this.id = id;
        this.companyId = companyId;
        this.bankName = bankName;
        this.accountNumber = accountNumber;
        this.status = status;
        this.lastSync = lastSync;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getCompanyId() { return companyId; }
    public void setCompanyId(UUID companyId) { this.companyId = companyId; }
    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }
    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getLastSync() { return lastSync; }
    public void setLastSync(String lastSync) { this.lastSync = lastSync; }
}
