package com.casrusil.siierpai.modules.bankingsync.domain.model;

import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class BankTransaction {
    private UUID id;
    private CompanyId companyId;
    private String bankName;
    private String accountNumber;
    private String transactionId; // ID provisto por Fintoc / Banco
    private LocalDateTime transactionDate;
    private String description;
    private BigDecimal amount; // Positivo (Ingreso), Negativo (Egreso)
    private String status; // UNRECONCILED, RECONCILED, IGNORED

    public BankTransaction(UUID id, CompanyId companyId, String bankName, String accountNumber, String transactionId, LocalDateTime transactionDate, String description, BigDecimal amount, String status) {
        this.id = id;
        this.companyId = companyId;
        this.bankName = bankName;
        this.accountNumber = accountNumber;
        this.transactionId = transactionId;
        this.transactionDate = transactionDate;
        this.description = description;
        this.amount = amount;
        this.status = status;
    }

    public static BankTransaction create(CompanyId companyId, String bankName, String accountNumber, String transactionId, LocalDateTime transactionDate, String description, BigDecimal amount) {
        return new BankTransaction(UUID.randomUUID(), companyId, bankName, accountNumber, transactionId, transactionDate, description, amount, "UNRECONCILED");
    }

    public void markAsReconciled() {
        this.status = "RECONCILED";
    }

    public void markAsIgnored() {
        this.status = "IGNORED";
    }

    public UUID getId() { return id; }
    public CompanyId getCompanyId() { return companyId; }
    public String getBankName() { return bankName; }
    public String getAccountNumber() { return accountNumber; }
    public String getTransactionId() { return transactionId; }
    public LocalDateTime getTransactionDate() { return transactionDate; }
    public String getDescription() { return description; }
    public BigDecimal getAmount() { return amount; }
    public String getStatus() { return status; }
}
