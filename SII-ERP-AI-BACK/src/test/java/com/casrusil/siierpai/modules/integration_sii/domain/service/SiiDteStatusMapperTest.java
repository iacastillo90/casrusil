package com.casrusil.siierpai.modules.integration_sii.domain.service;

import com.casrusil.siierpai.modules.integration_sii.domain.model.SiiDteQueryResult;
import com.casrusil.siierpai.modules.integration_sii.domain.service.SiiDteStatusMapper.SiiDocumentStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SiiDteStatusMapperTest {

    private final SiiDteStatusMapper mapper = new SiiDteStatusMapper("EPR,DOK", "", "RCT,RFT,RCH");

    @Test
    void shouldMapAcceptedCodes() {
        assertEquals(SiiDocumentStatus.ACEPTADO, mapper.map(result("DOK", "Documento aceptado")));
        assertEquals(SiiDocumentStatus.ACEPTADO, mapper.map(result("EPR", "Envio procesado")));
    }

    @Test
    void shouldMapAcceptedWithRepairsWhenGlosaSaysSo() {
        assertEquals(SiiDocumentStatus.ACEPTADO_CON_REPAROS,
                mapper.map(result("DOK", "Aceptado con reparos")));
    }

    @Test
    void shouldMapRejectedCodes() {
        assertEquals(SiiDocumentStatus.RECHAZADO, mapper.map(result("RCH", "Rechazado")));
        assertEquals(SiiDocumentStatus.RECHAZADO, mapper.map(result("RFT", "Firma invalida")));
    }

    @Test
    void shouldKeepPendingOnUnknownCode() {
        assertEquals(SiiDocumentStatus.PENDIENTE, mapper.map(result("XYZ", "desconocido")));
        assertEquals(SiiDocumentStatus.PENDIENTE, mapper.map(null));
    }

    private SiiDteQueryResult result(String estado, String glosa) {
        return new SiiDteQueryResult("000", "OK", estado, glosa, "<raw/>");
    }
}
