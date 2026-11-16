package com.casrusil.siierpai.modules.integration_sii.infrastructure.adapter.in.rest.dto;

import com.casrusil.siierpai.modules.integration_sii.domain.model.SiiSyncState;
import com.casrusil.siierpai.modules.integration_sii.domain.model.SiiSyncStatusValue;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Response body for the SII integration status endpoint.
 *
 * @param lastSync ISO-8601 timestamp of the last synchronization attempt
 * @param nextSync ISO-8601 timestamp of the next suggested synchronization
 * @param modules  per-module health, always containing {@code sii}, {@code caf} and {@code dte}
 * @param lastError last failure message, or {@code null}
 */
public record SiiSyncStatusDto(
        String lastSync,
        String nextSync,
        Map<String, String> modules,
        String lastError) {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    public static SiiSyncStatusDto from(SiiSyncState state) {
        Map<String, String> modules = new LinkedHashMap<>();
        modules.put("sii", name(state.siiStatus()));
        modules.put("caf", name(state.cafStatus()));
        modules.put("dte", name(state.dteStatus()));

        return new SiiSyncStatusDto(
                format(state.lastSyncAt()),
                format(state.nextSyncAt()),
                modules,
                state.lastError());
    }

    private static String format(LocalDateTime value) {
        return value != null ? value.format(FORMATTER) : null;
    }

    private static String name(SiiSyncStatusValue value) {
        return value != null ? value.name() : SiiSyncStatusValue.PENDING.name();
    }
}
