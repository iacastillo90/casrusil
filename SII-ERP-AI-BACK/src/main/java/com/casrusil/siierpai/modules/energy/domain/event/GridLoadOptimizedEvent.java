package com.casrusil.siierpai.modules.energy.domain.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Evento de Dominio: Optimización de Carga de Red.
 * 
 * <p>
 * Se emite cuando el sistema de Smart Load Balancing toma una acción para
 * optimizar el consumo de energía (Peak Shaving, Load Balancing, etc.).
 * 
 * <h2>Propósito</h2>
 * <ul>
 * <li><b>Auditoría:</b> Registrar todas las acciones de optimización</li>
 * <li><b>Contabilidad:</b> Calcular ahorros por evitar multas de demanda</li>
 * <li><b>Reporting:</b> Generar reportes de eficiencia energética</li>
 * <li><b>AI Training:</b> Datos para mejorar algoritmos de optimización</li>
 * </ul>
 * 
 * @see com.casrusil.siierpai.modules.energy.application.service.SmartLoadBalancingService
 * @since 2.0 - God Mode
 */
public final class GridLoadOptimizedEvent implements DomainEvent {

    private final UUID eventId;
    private final LocalDateTime occurredOn;
    private final BigDecimal totalConsumptionKw;
    private final BigDecimal contractedLimitKw;
    private final int powerReductionPercentage;
    private final String optimizationReason;

    public GridLoadOptimizedEvent(
            BigDecimal totalConsumptionKw,
            BigDecimal contractedLimitKw,
            int powerReductionPercentage,
            String optimizationReason) {
        this.eventId = UUID.randomUUID();
        this.occurredOn = LocalDateTime.now();
        this.totalConsumptionKw = Objects.requireNonNull(totalConsumptionKw);
        this.contractedLimitKw = Objects.requireNonNull(contractedLimitKw);
        this.powerReductionPercentage = powerReductionPercentage;
        this.optimizationReason = Objects.requireNonNull(optimizationReason);
    }

    @Override
    public UUID getEventId() {
        return eventId;
    }

    @Override
    public LocalDateTime getOccurredOn() {
        return occurredOn;
    }

    @Override
    public UUID getAggregateId() {
        return eventId; // No hay agregado específico, el evento es el agregado
    }

    public BigDecimal getTotalConsumptionKw() {
        return totalConsumptionKw;
    }

    public BigDecimal getContractedLimitKw() {
        return contractedLimitKw;
    }

    public int getPowerReductionPercentage() {
        return powerReductionPercentage;
    }

    public String getOptimizationReason() {
        return optimizationReason;
    }

    /**
     * Calcula el ahorro estimado por evitar multa de demanda.
     * 
     * @param penaltyCostPerKw Costo de penalización por kW excedido
     * @return Ahorro estimado en la moneda local
     */
    public BigDecimal calculateEstimatedSavings(BigDecimal penaltyCostPerKw) {
        BigDecimal excessKw = totalConsumptionKw.subtract(contractedLimitKw);
        if (excessKw.compareTo(BigDecimal.ZERO) > 0) {
            return excessKw.multiply(penaltyCostPerKw);
        }
        return BigDecimal.ZERO;
    }

    @Override
    public String toString() {
        return String.format(
                "GridLoadOptimizedEvent[consumption=%.2f kW, limit=%.2f kW, reduction=%d%%, reason=%s]",
                totalConsumptionKw, contractedLimitKw, powerReductionPercentage, optimizationReason);
    }
}
