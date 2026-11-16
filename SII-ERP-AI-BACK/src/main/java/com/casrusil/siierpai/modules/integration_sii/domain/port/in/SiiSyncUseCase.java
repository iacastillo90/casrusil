package com.casrusil.siierpai.modules.integration_sii.domain.port.in;

import com.casrusil.siierpai.modules.integration_sii.domain.model.SiiSyncResult;
import com.casrusil.siierpai.modules.integration_sii.domain.model.SiiSyncState;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;

/**
 * Inbound port for SII status and on-demand synchronization.
 */
public interface SiiSyncUseCase {

    /**
     * Returns the stored synchronization state, or a derived state when the
     * company has never synchronized.
     */
    SiiSyncState getStatus(CompanyId companyId);

    /**
     * Refreshes the SII token and validates RCV connectivity, then persists
     * the resulting state. RCV rows are not imported as invoices.
     */
    SiiSyncResult synchronize(CompanyId companyId);
}
