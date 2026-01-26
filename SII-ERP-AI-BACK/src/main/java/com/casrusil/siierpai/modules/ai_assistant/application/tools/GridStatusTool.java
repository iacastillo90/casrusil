package com.casrusil.siierpai.modules.ai_assistant.application.tools;

import dev.langchain4j.agent.tool.Tool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Herramienta de IA: Consulta de Estado de Red Eléctrica.
 * 
 * <h2>Propósito (CRÍTICO para CORFO)</h2>
 * <p>
 * Permite al asistente de IA (LLM) consultar el estado actual de la red
 * eléctrica
 * y responder preguntas como:
 * <ul>
 * <li>"¿Cuál es el consumo actual de energía?"</li>
 * <li>"¿Estamos cerca del límite de potencia?"</li>
 * <li>"¿Hay riesgo de multa por exceso de demanda?"</li>
 * <li>"¿Cuánta capacidad disponible tenemos?"</li>
 * </ul>
 * 
 * <h2>Integración con Smart Load Balancing</h2>
 * <p>
 * Esta herramienta complementa {@code SmartLoadBalancingService} permitiendo
 * que el usuario consulte al AI sobre el estado de la red antes de tomar
 * decisiones.
 * 
 * <h2>Ejemplo de Conversación</h2>
 * 
 * <pre>
 * Usuario: "¿Podemos agregar 3 cargadores más sin exceder el límite?"
 * AI: [usa esta herramienta] "Actualmente consumimos 135 kW de 150 kW contratados.
 *      Tenemos 15 kW disponibles. 3 cargadores de 7.4 kW = 22.2 kW.
 *      ⚠️ Excederíamos el límite. Recomiendo agregar máximo 2 cargadores."
 * </pre>
 * 
 * @see com.casrusil.siierpai.modules.energy.application.service.SmartLoadBalancingService
 * @since 2.0 - God Mode
 */
@Component
public class GridStatusTool {

    @Value("${energy.grid.max-site-power-kw:150.0}")
    private double maxSitePowerKw;

    /**
     * Obtiene el estado actual de la red eléctrica del sitio.
     * 
     * <p>
     * Esta herramienta es llamada automáticamente por el LLM cuando el usuario
     * hace preguntas sobre el consumo de energía o la capacidad de la red.
     * 
     * @return Reporte detallado del estado de la red en formato legible
     */
    @Tool("Obtiene el estado actual de la red eléctrica: consumo, límite, capacidad disponible y sesiones activas")
    public String getGridStatus() {
        // Calcular consumo total actual
        double currentConsumptionKw = calculateCurrentConsumption();
        double availableCapacityKw = maxSitePowerKw - currentConsumptionKw;
        double utilizationPercentage = (currentConsumptionKw / maxSitePowerKw) * 100;

        // Obtener número de sesiones activas
        long activeSessions = getActiveSessionCount();

        // Construir reporte
        StringBuilder report = new StringBuilder();
        report.append("📊 Estado de la Red Eléctrica:\n\n");

        // Consumo actual
        report.append(String.format("⚡ Consumo Actual: %.2f kW\n", currentConsumptionKw));
        report.append(String.format("📈 Límite Contratado: %.2f kW\n", maxSitePowerKw));
        report.append(String.format("🔋 Capacidad Disponible: %.2f kW\n", availableCapacityKw));
        report.append(String.format("📊 Utilización: %.1f%%\n\n", utilizationPercentage));

        // Estado de riesgo
        if (currentConsumptionKw > maxSitePowerKw) {
            double excess = currentConsumptionKw - maxSitePowerKw;
            report.append(String.format("🚨 ALERTA: Excediendo límite por %.2f kW\n", excess));
            report.append("   → Sistema de Smart Load Balancing debería estar activo\n");
            report.append("   → Riesgo de multa por exceso de demanda\n\n");
        } else if (utilizationPercentage > 90) {
            report.append("⚠️ ADVERTENCIA: Utilización > 90%\n");
            report.append("   → Cerca del límite, evitar agregar más carga\n\n");
        } else if (utilizationPercentage > 75) {
            report.append("⚡ PRECAUCIÓN: Utilización > 75%\n");
            report.append("   → Monitorear antes de agregar más cargadores\n\n");
        } else {
            report.append("✅ NORMAL: Red operando dentro de parámetros seguros\n\n");
        }

        // Sesiones activas
        report.append(String.format("🔌 Sesiones de Carga Activas: %d\n", activeSessions));

        if (activeSessions > 0) {
            double avgPowerPerSession = currentConsumptionKw / activeSessions;
            report.append(String.format("   → Promedio por sesión: %.2f kW\n", avgPowerPerSession));
        }

        // Recomendaciones
        report.append("\n💡 Recomendaciones:\n");
        if (availableCapacityKw > 20) {
            int maxNewChargers = (int) (availableCapacityKw / 7.4); // Asumiendo 7.4 kW por cargador
            report.append(String.format("   ✅ Puedes agregar hasta %d cargadores de 7.4 kW\n", maxNewChargers));
        } else if (availableCapacityKw > 0) {
            report.append("   ⚠️ Capacidad limitada, evaluar antes de agregar carga\n");
        } else {
            report.append("   🚫 Sin capacidad disponible, reducir carga actual primero\n");
        }

        return report.toString();
    }

    /**
     * Calcula el consumo actual total del sitio.
     * En producción, suma el consumo de todas las sesiones activas.
     */
    private double calculateCurrentConsumption() {
        // TODO: Implementación real
        // return sessionRepository.findAllActive().stream()
        // .map(session -> session.getCurrentPowerKw())
        // .reduce(BigDecimal.ZERO, BigDecimal::add)
        // .doubleValue();

        // Simulación para demo
        return 135.0 + (Math.random() * 20.0); // 135-155 kW
    }

    /**
     * Obtiene el número de sesiones de carga activas.
     */
    private long getActiveSessionCount() {
        // TODO: Implementación real
        // return sessionRepository.countAllActive();

        // Simulación para demo
        return 5 + (long) (Math.random() * 3); // 5-7 sesiones
    }
}
