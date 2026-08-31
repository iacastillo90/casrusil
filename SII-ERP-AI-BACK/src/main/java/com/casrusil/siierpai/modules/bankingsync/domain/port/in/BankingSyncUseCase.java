package com.casrusil.siierpai.modules.bankingsync.domain.port.in;

import com.casrusil.siierpai.modules.bankingsync.domain.model.BankConnection;
import com.casrusil.siierpai.modules.bankingsync.domain.model.BankTransaction;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;

import java.util.List;
import java.util.UUID;

public interface BankingSyncUseCase {
    BankConnection connectBank(CompanyId companyId, String bankName, String accountNumber);
    List<BankConnection> getConnections(CompanyId companyId);
    
    void syncTransactions(CompanyId companyId);
    List<BankTransaction> getTransactions(CompanyId companyId);
    
    void reconcileTransaction(UUID transactionId);
    void ignoreTransaction(UUID transactionId);
}
