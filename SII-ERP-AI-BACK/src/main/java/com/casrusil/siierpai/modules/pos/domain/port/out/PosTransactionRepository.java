package com.casrusil.siierpai.modules.pos.domain.port.out;

import com.casrusil.siierpai.modules.pos.domain.model.PosTransaction;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PosTransactionRepository {
    PosTransaction save(PosTransaction transaction);
    Optional<PosTransaction> findById(UUID id);
    List<PosTransaction> findByCompanyId(CompanyId companyId);
    List<PosTransaction> findPendingSync(CompanyId companyId);
}
