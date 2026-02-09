package com.casrusil.siierpai.modules.energy.application.service;

import com.casrusil.siierpai.modules.energy.domain.event.GridLoadOptimizedEvent;
import com.casrusil.siierpai.modules.energy.domain.port.out.ChargingStationControlPort;
import com.casrusil.siierpai.modules.energy.domain.port.out.EventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Servicio de Aplicación: Balanceo Inteligente de Carga (Smart Load Balancing).
 * 
 * <h2>Propósito de Negocio (CRÍTICO para CORFO)</h2>
 * <p>
 * Implementa algoritmos de optimización de consumo eléctrico para demostrar
 * <b>"Gestión Inteligente de Energía"</b> (no solo monitoreo). Este servicio
 * toma decisiones automáticas para evitar multas por exceso de demanda y
 * optimizar costos operativos.
 * 
 * <h2>Algoritmos Implementados</h2>
 * <ul>
 * <li><b>Peak Shaving:</b> Aplanamiento de curva de demanda</li>
 * <li><b>Demand Response:</b> Respuesta automática a límites de red</li>
 * <li><b>Cost Optimization:</b> Minimización de cargos por demanda</li>
 * </ul>
 * 
 * <h2>Flujo de Operación</h2>
 * <ol>
 * <li>Recibe consumo total actual del sitio</li>
 * <li>Compara con límite de potencia contratada</li>
 * <li>Si excede: reduce potencia de cargadores no prioritarios</li>
 * <li>Emite evento de auditoría para contabilidad</li>
 * </ol>
 * 
 * <h2>Impacto Financiero</h2>
 * <p>
 * En Chile, exceder la potencia contratada puede generar multas de hasta
 * $50.000 CLP por kW excedido. Este servicio previene estas multas
 * automáticamente.
 * 
 * <h2>Arquitectura</h2>
 * <ul>
 * <li><b>Patrón:</b> Application Service (Hexagonal Architecture)</li>
 * <li><b>Trigger:</b> {@code SmartLoadBalancingListener} vía eventos</li>
 * <li><b>Control:</b> {@code ChargingStationControlPort} → OCPP</li>
 * </ul>
 * 
 * @see com.casrusil.siierpai.modules.energy.application.listener.SmartLoadBalancingListener
 * @see com.casrusil.siierpai.modules.energy.domain.port.out.ChargingStationControlPort
 * @since 2.0 - God Mode
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SmartLoadBalancingService {

    private final ChargingStationControlPort stationControl;
    private final EventPublisher eventPublisher;

    @Value("${energy.grid.max-site-power-kw:150.0}")
    private double maxSitePowerKw;

    @Value("${energy.grid.power-reduction-percentage:50}")
    private int powerReductionPercentage;

    @Value("${energy.grid.demand-penalty-per-kw:50000}")
    private double demandPenaltyPerKw; // CLP por kW excedido

    /**
     * Algoritmo de Peak Shaving (Aplanamiento de Curva de Demanda).
     * 
     * <p>
     * <b>NIVEL DIOS:</b> Este método toma decisiones automáticas sin intervención
     * humana para optimizar el consumo de energía y evitar multas.
     * 
     * <h3>Lógica de Decisión</h3>
     * 
     * <pre>
     * SI consumo_actual > límite_contratado ENTONCES
     *     reducir_potencia_cargadores(50%)
     *     registrar_evento_optimización()
     *     calcular_ahorro_estimado()
     * FIN SI
     * </pre>
     * 
     * <h3>Ejemplo de Uso</h3>
     * 
     * <pre>
     * // Llamado desde SmartLoadBalancingListener
     * service.optimizeGridLoad(165.5); // 165.5 kW actual
     * // → Reduce potencia si excede 150 kW
     * </pre>
     * 
     * @param currentTotalConsumptionKw Consumo total actual del sitio en kW
     */
    public void optimizeGridLoad(double currentTotalConsumptionKw) {
        BigDecimal consumption = BigDecimal.valueOf(currentTotalConsumptionKw);
        BigDecimal limit = BigDecimal.valueOf(maxSitePowerKw);

        if (currentTotalConsumptionKw > maxSitePowerKw) {
            // 🚨 ALERTA: Exceso de potencia detectado
            double excessKw = currentTotalConsumptionKw - maxSitePowerKw;

            log.warn("⚠️ ALERTA DE POTENCIA: Consumo actual ({} kW) excede el límite contratado ({} kW) por {} kW",
                    currentTotalConsumptionKw, maxSitePowerKw, excessKw);

            // 🤖 ACCIÓN AUTOMÁTICA: Reducir potencia
            log.info("🤖 AI Action: Reduciendo potencia en cargadores de baja prioridad al {}%...",
                    powerReductionPercentage);

            try {
                stationControl.broadcastPowerLimit(powerReductionPercentage);
                log.info("✅ Comando OCPP enviado: SetChargingProfile(limit={}%)", powerReductionPercentage);

                // Emitir evento para auditoría y contabilidad
                GridLoadOptimizedEvent event = new GridLoadOptimizedEvent(
                        consumption,
                        limit,
                        powerReductionPercentage,
                        "Peak Shaving - Exceso de demanda detectado");

                eventPublisher.publish(event);

                // Calcular ahorro estimado
                BigDecimal estimatedSavings = event.calculateEstimatedSavings(
                        BigDecimal.valueOf(demandPenaltyPerKw));

                log.info("💰 Ahorro estimado por evitar multa: ${} CLP", estimatedSavings);

            } catch (Exception e) {
                log.error("🔴 Error al ejecutar optimización de carga: {}", e.getMessage(), e);
                // No lanzamos excepción para no afectar el flujo de carga
            }

        } else {
            // ✅ Red estable, consumo dentro de límites
            log.debug("✅ Red estable. Consumo: {} kW (Límite: {} kW)",
                    currentTotalConsumptionKw, maxSitePowerKw);
        }
    }

    /**
     * Restaura la potencia máxima cuando el consumo vuelve a niveles normales.
     * 
     * <p>
     * Llamado cuando el consumo total cae por debajo del 80% del límite contratado,
     * permitiendo que los cargadores vuelvan a su potencia máxima.
     * 
     * @param currentTotalConsumptionKw Consumo total actual del sitio en kW
     */
    public void restorePowerIfSafe(double currentTotalConsumptionKw) {
        double safeThreshold = maxSitePowerKw * 0.8; // 80% del límite

        if (currentTotalConsumptionKw < safeThreshold) {
            log.info("✅ Consumo estable ({} kW). Restaurando potencia máxima en cargadores...",
                    currentTotalConsumptionKw);

            try {
                stationControl.removeAllPowerLimits();
                log.info("✅ Potencia máxima restaurada en todos los cargadores");
            } catch (Exception e) {
                log.error("🔴 Error al restaurar potencia: {}", e.getMessage(), e);
            }
        }
    }
}
