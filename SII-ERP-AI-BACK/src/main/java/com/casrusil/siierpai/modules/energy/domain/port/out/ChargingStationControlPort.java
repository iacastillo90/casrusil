package com.casrusil.siierpai.modules.energy.domain.port.out;

import com.casrusil.siierpai.modules.energy.domain.model.StationId;

/**
 * Puerto de Salida: Control de Estaciones de Carga.
 * 
 * <h2>Propósito de Negocio</h2>
 * <p>
 * Permite al sistema enviar comandos de control a las estaciones de carga para
 * optimizar el consumo de energía. Este puerto es crítico para implementar
 * estrategias de "Peak Shaving" (aplanamiento de curva de demanda) y evitar
 * multas por exceso de potencia contratada.
 * 
 * <h2>Casos de Uso</h2>
 * <ul>
 * <li><b>Peak Shaving:</b> Reducir potencia cuando se excede el límite
 * contratado</li>
 * <li><b>Load Balancing:</b> Distribuir carga entre múltiples cargadores</li>
 * <li><b>Demand Response:</b> Responder a señales de la red eléctrica</li>
 * <li><b>Cost Optimization:</b> Cargar en horarios de tarifa baja</li>
 * </ul>
 * 
 * <h2>Integración OCPP</h2>
 * <p>
 * La implementación de este puerto debe enviar comandos OCPP a los cargadores:
 * <ul>
 * <li><b>SetChargingProfile:</b> Limitar potencia máxima</li>
 * <li><b>RemoteStartTransaction:</b> Iniciar carga con perfil específico</li>
 * <li><b>RemoteStopTransaction:</b> Detener carga de emergencia</li>
 * </ul>
 * 
 * <h2>Arquitectura</h2>
 * <ul>
 * <li><b>Patrón:</b> Hexagonal Architecture - Output Port</li>
 * <li><b>Implementación:</b> {@code OcppChargingStationControlAdapter} en
 * infrastructure</li>
 * <li><b>Protocolo:</b> OCPP 1.6/2.0 - SetChargingProfile</li>
 * </ul>
 * 
 * @see com.casrusil.siierpai.modules.energy.application.service.SmartLoadBalancingService
 * @since 2.0 - God Mode
 */
public interface ChargingStationControlPort {

    /**
     * Reduce la potencia de todos los cargadores activos a un porcentaje del
     * máximo.
     * 
     * <p>
     * Este método es usado por el algoritmo de Peak Shaving cuando el consumo total
     * del sitio excede el límite contratado. Envía un comando OCPP
     * SetChargingProfile
     * a todos los cargadores activos.
     * 
     * <h3>Ejemplo de Uso</h3>
     * 
     * <pre>
     * // Reducir a 50% cuando se excede el límite
     * stationControl.broadcastPowerLimit(50);
     * </pre>
     * 
     * @param percentageLimit Porcentaje de potencia máxima (0-100)
     * @throws IllegalArgumentException si percentageLimit < 0 o > 100
     */
    void broadcastPowerLimit(int percentageLimit);

    /**
     * Establece un límite de potencia específico para una estación.
     * 
     * <p>
     * Permite control granular de estaciones individuales, útil para priorizar
     * cargadores críticos (ej: flotas de emergencia) sobre cargadores de empleados.
     * 
     * @param stationId ID de la estación a controlar
     * @param maxKw     Potencia máxima en kilovatios
     * @throws IllegalArgumentException si maxKw < 0
     */
    void setStationPowerLimit(StationId stationId, double maxKw);

    /**
     * Restaura la potencia máxima en todos los cargadores.
     * 
     * <p>
     * Usado cuando el consumo vuelve a niveles normales y se puede remover
     * la restricción de potencia.
     */
    void removeAllPowerLimits();
}
