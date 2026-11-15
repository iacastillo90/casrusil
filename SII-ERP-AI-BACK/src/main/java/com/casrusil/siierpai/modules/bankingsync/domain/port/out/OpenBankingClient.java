package com.casrusil.siierpai.modules.bankingsync.domain.port.out;

import com.casrusil.siierpai.modules.bankingsync.domain.model.BankTransaction;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;

import java.util.List;

public interface OpenBankingClient {
    List<BankTransaction> fetchTransactions(CompanyId companyId, String bankName, String accountNumber);
}
