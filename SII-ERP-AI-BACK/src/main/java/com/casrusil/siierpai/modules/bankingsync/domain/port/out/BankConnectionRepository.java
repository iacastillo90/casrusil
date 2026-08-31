package com.casrusil.siierpai.modules.bankingsync.domain.port.out;

import com.casrusil.siierpai.modules.bankingsync.domain.model.BankConnection;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BankConnectionRepository {
    BankConnection save(BankConnection connection);
    Optional<BankConnection> findById(UUID id);
    List<BankConnection> findByCompanyId(CompanyId companyId);
}
