package com.casrusil.siierpai.modules.bankingsync.domain.port.out;

import com.casrusil.siierpai.modules.bankingsync.domain.model.BankTransaction;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BankTransactionRepository {
    BankTransaction save(BankTransaction transaction);
    Optional<BankTransaction> findById(UUID id);
    List<BankTransaction> findByCompanyId(CompanyId companyId);
}
