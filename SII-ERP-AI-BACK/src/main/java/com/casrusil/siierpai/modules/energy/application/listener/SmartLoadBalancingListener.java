package com.casrusil.siierpai.modules.energy.application.listener;

import com.casrusil.siierpai.modules.energy.application.service.SmartLoadBalancingService;
import com.casrusil.siierpai.modules.energy.domain.event.EnergyConsumedEvent;
import com.casrusil.siierpai.modules.energy.domain.port.out.ChargingSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Listener de Eventos: Smart Load Balancing.
 * 
 * <h2>Propósito</h2>
 * <p>
 * Escucha eventos de consumo de energía ({@code EnergyConsumedEvent}) y dispara
 * el algoritmo de optimización de carga cuando es necesario. Este listener es
 * el "cerebro" que conecta el monitoreo en tiempo real con las decisiones
 * automáticas de control.
 * 
 * <h2>Flujo de Operación</h2>
 * <ol>
 * <li>Recibe {@code EnergyConsumedEvent} cada ~60 segundos</li>
 * <li>Calcula consumo total del sitio sumando todas las sesiones activas</li>
 * <li>Llama a {@code SmartLoadBalancingService.optimizeGridLoad()}</li>
 * <li>El servicio decide si reducir potencia o restaurarla</li>
 * </ol>
 * 
 * <h2>Procesamiento Asíncrono</h2>
 * <p>
 * Usa {@code @Async} con Virtual Threads para no bloquear el flujo OCPP.
 * Esto es crítico porque los cargadores esperan respuesta rápida a MeterValues.
 * 
 * @see SmartLoadBalancingService
 * @see EnergyConsumedEvent
 * @since 2.0 - God Mode
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SmartLoadBalancingListener {

    private final SmartLoadBalancingService loadBalancingService;
    private final ChargingSessionRepository sessionRepository;

    /**
     * Maneja eventos de consumo de energía para optimizar la carga de red.
     * 
     * <p>
     * Este método se ejecuta asíncronamente en un Virtual Thread cada vez que
     * un cargador reporta su consumo (típicamente cada 60 segundos).
     * 
     * @param event Evento de consumo de energía
     */
    @Async
    @EventListener
    public void handleEnergyConsumed(EnergyConsumedEvent event) {
        log.debug("📊 Evaluando carga de red: sessionId={}, cumulative={} kWh",
                event.getSessionId(), event.getCumulativeEnergyKwh());

        try {
            // Calcular consumo total del sitio
            double totalConsumptionKw = calculateTotalSiteConsumption();

            log.debug("📈 Consumo total del sitio: {} kW", totalConsumptionKw);

            // Disparar optimización si es necesario
            loadBalancingService.optimizeGridLoad(totalConsumptionKw);

            // También verificar si podemos restaurar potencia
            loadBalancingService.restorePowerIfSafe(totalConsumptionKw);

        } catch (Exception e) {
            log.error("🔴 Error al evaluar carga de red: {}", e.getMessage(), e);
            // No lanzamos excepción para no afectar el flujo de carga
        }
    }

    /**
     * Calcula el consumo total del sitio sumando todas las sesiones activas.
     * 
     * <p>
     * En producción, esto consultaría la base de datos para obtener todas las
     * sesiones activas y sumar su consumo instantáneo. Para demo, usamos un
     * valor simulado.
     * 
     * @return Consumo total en kW
     */
    private double calculateTotalSiteConsumption() {
        // TODO: Implementación real
        // List<ChargingSession> activeSessions = sessionRepository.findAllActive();
        // return activeSessions.stream()
        // .map(session -> session.getCurrentPowerKw())
        // .reduce(BigDecimal.ZERO, BigDecimal::add)
        // .doubleValue();

        // Simulación para demo: consumo variable entre 120-180 kW
        double baseConsumption = 140.0;
        double variation = (Math.random() - 0.5) * 40.0; // ±20 kW
        return baseConsumption + variation;
    }
}
