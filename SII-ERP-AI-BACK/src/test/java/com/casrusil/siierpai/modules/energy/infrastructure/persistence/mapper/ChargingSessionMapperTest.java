package com.casrusil.siierpai.modules.energy.infrastructure.persistence.mapper;

import com.casrusil.siierpai.modules.energy.domain.model.ChargingSession;
import com.casrusil.siierpai.modules.energy.domain.model.ChargingSessionStatus;
import com.casrusil.siierpai.modules.energy.infrastructure.persistence.entity.ChargingSessionEntity;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Rehidratación de sesiones persistidas: antes, al leer una sesión completada
 * el mapper llamaba {@code startCharging} y lanzaba "La sesión ya fue iniciada"
 * (409 en /energy/metrics). Además pisaba {@code endedAt} con la hora actual.
 */
class ChargingSessionMapperTest {

    private final ChargingSessionMapper mapper = new ChargingSessionMapper();

    @Test
    void completedSessionIsRestoredWithoutTransitionsAndKeepsTimes() {
        LocalDateTime started = LocalDateTime.now().minusMinutes(90);
        LocalDateTime ended = LocalDateTime.now().minusMinutes(30);

        ChargingSessionEntity entity = new ChargingSessionEntity(
                UUID.randomUUID(), UUID.randomUUID(), "OCPP-1", 1, UUID.randomUUID(),
                ChargingSessionStatus.COMPLETED, started);
        entity.setEndedAt(ended);
        entity.setInitialEnergyKwh(new BigDecimal("0.000"));
        entity.setFinalEnergyKwh(new BigDecimal("25.000"));
        entity.setStopReason("OK");

        ChargingSession session = mapper.toDomain(entity);

        assertEquals(ChargingSessionStatus.COMPLETED, session.getStatus());
        assertEquals(ended, session.getEndedAt());
        assertEquals(0, new BigDecimal("25.000").compareTo(session.getTotalEnergyConsumed()));
        assertTrue(session.getDuration().toMinutes() >= 59 && session.getDuration().toMinutes() <= 61);
    }

    @Test
    void activeSessionKeepsNullEnd() {
        ChargingSessionEntity entity = new ChargingSessionEntity(
                UUID.randomUUID(), UUID.randomUUID(), "OCPP-2", 1, UUID.randomUUID(),
                ChargingSessionStatus.CHARGING, LocalDateTime.now().minusMinutes(5));
        entity.setInitialEnergyKwh(new BigDecimal("1.000"));

        ChargingSession session = mapper.toDomain(entity);

        assertEquals(ChargingSessionStatus.CHARGING, session.getStatus());
        assertEquals(null, session.getEndedAt());
    }
}
