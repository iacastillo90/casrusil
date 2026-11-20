package com.casrusil.siierpai.modules.energy.application.service;

import com.casrusil.siierpai.modules.energy.domain.model.ChargingSession;
import com.casrusil.siierpai.modules.energy.domain.model.ChargingStation;
import com.casrusil.siierpai.modules.energy.domain.model.ConnectorId;
import com.casrusil.siierpai.modules.energy.domain.model.EnergyMeasurement;
import com.casrusil.siierpai.modules.energy.domain.model.OcppIdentifier;
import com.casrusil.siierpai.modules.energy.domain.model.SessionId;
import com.casrusil.siierpai.modules.energy.domain.model.StationId;
import com.casrusil.siierpai.modules.energy.domain.port.out.AlertRepository;
import com.casrusil.siierpai.modules.energy.domain.port.out.ChargingSessionRepository;
import com.casrusil.siierpai.modules.energy.domain.port.out.ChargingStationRepository;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import com.casrusil.siierpai.shared.infrastructure.context.CompanyContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Métricas de energía: pico real a partir de sesiones de carga y filtrado por
 * empresa (no se deben leer sesiones de otras empresas).
 */
class EnergyQueryServiceTest {

    @Mock
    private ChargingStationRepository stationRepository;
    @Mock
    private ChargingSessionRepository sessionRepository;
    @Mock
    private AlertRepository alertRepository;

    private EnergyQueryService service;
    private AutoCloseable mocks;
    private final CompanyId companyId = CompanyId.random();

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        service = new EnergyQueryService(stationRepository, sessionRepository, alertRepository);
    }

    @AfterEach
    void tearDown() throws Exception {
        mocks.close();
    }

    private ChargingStation station(UUID id) {
        return ChargingStation.builder()
                .stationId(StationId.of(id))
                .name("Estación")
                .location(com.casrusil.siierpai.modules.energy.domain.model.StationLocation.of(
                        -33.43, -70.61, "Av. Siempre Viva 1", "Santiago", "CL"))
                .createdAt(LocalDateTime.now())
                .companyId(companyId.value())
                .status(com.casrusil.siierpai.modules.energy.domain.model.StationStatus.OPERATIONAL)
                .active(true)
                .build();
    }

    /**
     * Sesión completada ahora, iniciada hace {@code minutes} minutos y con
     * {@code kwh} de energía acumulada. La potencia promedio resulta
     * {@code kwh / (minutes/60)}.
     */
    private ChargingSession session(LocalDateTime start, int minutes, double kwh) {
        ChargingSession session = ChargingSession.builder()
                .sessionId(SessionId.generate())
                .stationId(StationId.generate())
                .chargerOcppId(OcppIdentifier.of("OCPP-TEST"))
                .connectorId(ConnectorId.of(1))
                .userId(UUID.randomUUID())
                .startedAt(start)
                .build();
        session.startCharging(EnergyMeasurement.of(BigDecimal.ZERO, start));
        session.complete(EnergyMeasurement.of(BigDecimal.valueOf(kwh), start.plusMinutes(minutes)), "OK");
        return session;
    }

    @Test
    void peakIsMaxAveragePowerOfTodaySessions() throws Exception {
        UUID stationId = UUID.randomUUID();
        ChargingStation station = station(stationId);
        ChargingSession slow = session(LocalDateTime.now().minusMinutes(60), 60, 22.0); // 22 kW
        ChargingSession fast = session(LocalDateTime.now().minusMinutes(30), 30, 25.0); // 50 kW

        when(stationRepository.findByCompanyId(companyId.value())).thenReturn(List.of(station));
        when(sessionRepository.findCompletedByStationIds(anyList(), any(), any()))
                .thenReturn(List.of(slow, fast));

        Double peak = ScopedValue.where(CompanyContext.COMPANY_ID, companyId).call(
                service::getPeakAveragePowerTodayByCompany);

        assertEquals(50.0, peak, 2.0);
        verify(sessionRepository, never()).findCompletedBetween(any(), any());
    }

    @Test
    void peakIsNullWhenThereAreNoSessions() throws Exception {
        when(stationRepository.findByCompanyId(companyId.value())).thenReturn(List.of(station(UUID.randomUUID())));
        when(sessionRepository.findCompletedByStationIds(anyList(), any(), any())).thenReturn(List.of());

        Double peak = ScopedValue.where(CompanyContext.COMPANY_ID, companyId).call(
                service::getPeakAveragePowerTodayByCompany);

        assertNull(peak, "Sin sesiones no se inventa un pico de 0");
    }

    @Test
    void peakIsNullWhenSessionsHaveNoMeasurableDuration() throws Exception {
        when(stationRepository.findByCompanyId(companyId.value())).thenReturn(List.of(station(UUID.randomUUID())));
        when(sessionRepository.findCompletedByStationIds(anyList(), any(), any()))
                .thenReturn(List.of(session(LocalDateTime.now(), 0, 10.0)));

        Double peak = ScopedValue.where(CompanyContext.COMPANY_ID, companyId).call(
                service::getPeakAveragePowerTodayByCompany);

        assertNull(peak);
    }

    @Test
    void todayConsumptionUsesCompanyStationsOnly() throws Exception {
        when(stationRepository.findByCompanyId(companyId.value())).thenReturn(List.of(station(UUID.randomUUID())));
        when(sessionRepository.findCompletedByStationIds(anyList(), any(), any()))
                .thenReturn(List.of(session(LocalDateTime.now().minusHours(1), 60, 12.5)));

        double consumption = ScopedValue.where(CompanyContext.COMPANY_ID, companyId).call(
                service::getTodayConsumptionByCompany);

        assertEquals(12.5, consumption, 0.001);
        verify(sessionRepository, never()).findCompletedBetween(any(), any());
    }

    @Test
    void recentSessionsUseCompanyStationsOnly() {
        UUID stationId = UUID.randomUUID();
        when(stationRepository.findByCompanyId(companyId.value())).thenReturn(List.of(station(stationId)));
        when(sessionRepository.findRecentByStationIds(anyList(), anyInt())).thenReturn(List.of());

        assertDoesNotThrow(() -> ScopedValue.where(CompanyContext.COMPANY_ID, companyId).call(
                () -> service.getRecentSessionsByCompany(10)));
        verify(sessionRepository, never()).findCompletedBetween(any(), any());
    }
}
