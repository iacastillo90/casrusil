package com.casrusil.siierpai.modules.bankingsync.domain.model;

import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import java.util.UUID;

public class BankConnection {
    private UUID id;
    private CompanyId companyId;
    private String bankName;
    private String accountNumber;
    private String status; // ACTIVE, ERROR, DISCONNECTED
    private String lastSync;

    public BankConnection(UUID id, CompanyId companyId, String bankName, String accountNumber, String status, String lastSync) {
        this.id = id;
        this.companyId = companyId;
        this.bankName = bankName;
        this.accountNumber = accountNumber;
        this.status = status;
        this.lastSync = lastSync;
    }

    public static BankConnection create(CompanyId companyId, String bankName, String accountNumber) {
        return new BankConnection(UUID.randomUUID(), companyId, bankName, accountNumber, "ACTIVE", "N/A");
    }

    public UUID getId() { return id; }
    public CompanyId getCompanyId() { return companyId; }
    public String getBankName() { return bankName; }
    public String getAccountNumber() { return accountNumber; }
    public String getStatus() { return status; }
    public String getLastSync() { return lastSync; }
    public void setStatus(String status) { this.status = status; }
    public void setLastSync(String lastSync) { this.lastSync = lastSync; }
}
