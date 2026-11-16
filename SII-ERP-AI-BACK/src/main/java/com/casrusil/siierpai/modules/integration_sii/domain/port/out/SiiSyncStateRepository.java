package com.casrusil.siierpai.modules.integration_sii.domain.port.out;

import com.casrusil.siierpai.modules.integration_sii.domain.model.SiiSyncState;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;

import java.util.Optional;

/**
 * Persistence port for the per-company SII synchronization state.
 */
public interface SiiSyncStateRepository {

    Optional<SiiSyncState> findByCompanyId(CompanyId companyId);

    SiiSyncState save(SiiSyncState state);
}
