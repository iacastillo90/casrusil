package com.casrusil.siierpai.modules.integration_sii.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CafInfoTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 1, 15);

    @Test
    void evaluateEstado_shouldReturnAgotadoWhenNoFoliosLeft() {
        assertEquals(CafInfo.EstadoCaf.AGOTADO, CafInfo.evaluateEstado(0L, null, TODAY, 10));
        assertEquals(CafInfo.EstadoCaf.AGOTADO, CafInfo.evaluateEstado(null, null, TODAY, 10));
    }

    @Test
    void evaluateEstado_shouldReturnPorAgotarseWhenBelowThreshold() {
        assertEquals(CafInfo.EstadoCaf.POR_AGOTARSE, CafInfo.evaluateEstado(10L, null, TODAY, 10));
        assertEquals(CafInfo.EstadoCaf.POR_AGOTARSE, CafInfo.evaluateEstado(3L, null, TODAY, 10));
    }

    @Test
    void evaluateEstado_shouldReturnVigenteWhenPlentyOfFolios() {
        assertEquals(CafInfo.EstadoCaf.VIGENTE, CafInfo.evaluateEstado(11L, null, TODAY, 10));
    }

    @Test
    void evaluateEstado_shouldReturnVencidoOnlyAfterExpirationDate() {
        assertEquals(CafInfo.EstadoCaf.VENCIDO,
                CafInfo.evaluateEstado(100L, LocalDate.of(2025, 12, 31), TODAY, 10));
    }

    @Test
    void evaluateEstado_shouldStayValidOnExpirationDate() {
        assertEquals(CafInfo.EstadoCaf.VIGENTE,
                CafInfo.evaluateEstado(100L, LocalDate.of(2026, 1, 15), TODAY, 10));
        assertEquals(CafInfo.EstadoCaf.VIGENTE,
                CafInfo.evaluateEstado(100L, LocalDate.of(2026, 1, 16), TODAY, 10));
    }
}
