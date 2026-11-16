package com.casrusil.siierpai.modules.integration_sii.domain.model;

import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;

import java.time.LocalDateTime;

/**
 * Persisted SII synchronization state for a single company.
 *
 * <p>The state is advisory: it reports the last connectivity check and the
 * health of each SII module. It does not represent imported documents.</p>
 *
 * @param companyId tenant that owns the state
 * @param lastSyncAt last successful or attempted synchronization
 * @param nextSyncAt cosmetic next scheduled synchronization (no scheduler)
 * @param siiStatus token/authentication health
 * @param cafStatus CAF availability health
 * @param dteStatus DTE import health
 * @param lastError last failure message, or {@code null}
 * @param updatedAt row update timestamp
 */
public record SiiSyncState(
        CompanyId companyId,
        LocalDateTime lastSyncAt,
        LocalDateTime nextSyncAt,
        SiiSyncStatusValue siiStatus,
        SiiSyncStatusValue cafStatus,
        SiiSyncStatusValue dteStatus,
        String lastError,
        LocalDateTime updatedAt) {
}
