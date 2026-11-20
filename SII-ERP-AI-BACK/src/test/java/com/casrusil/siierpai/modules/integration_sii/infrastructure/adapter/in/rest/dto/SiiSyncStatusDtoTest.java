package com.casrusil.siierpai.modules.integration_sii.infrastructure.adapter.in.rest.dto;

import com.casrusil.siierpai.modules.integration_sii.domain.model.SiiSyncState;
import com.casrusil.siierpai.modules.integration_sii.domain.model.SiiSyncStatusValue;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SiiSyncStatusDtoTest {

    @Test
    void shouldExposeSiiEnvironment() {
        SiiSyncState state = new SiiSyncState(
                CompanyId.random(),
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(1),
                SiiSyncStatusValue.SYNCED,
                SiiSyncStatusValue.PENDING,
                SiiSyncStatusValue.SYNCED,
                null,
                LocalDateTime.now());

        SiiSyncStatusDto dto = SiiSyncStatusDto.from(state, "produccion");

        assertEquals("produccion", dto.environment());
        assertEquals("SYNCED", dto.modules().get("sii"));
    }
}
