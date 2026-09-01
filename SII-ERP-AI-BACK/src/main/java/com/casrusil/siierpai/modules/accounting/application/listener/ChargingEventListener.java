package com.casrusil.siierpai.modules.accounting.application.listener;

import com.casrusil.siierpai.modules.accounting.domain.model.AccountConstants;
import com.casrusil.siierpai.modules.accounting.domain.model.AccountConstants;
import com.casrusil.siierpai.modules.accounting.domain.model.AccountingEntry;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import com.casrusil.siierpai.modules.accounting.domain.model.EntryType;
import com.casrusil.siierpai.modules.accounting.domain.service.AccountingEntryBuilder;
import com.casrusil.siierpai.modules.accounting.domain.service.AccountingEntryService;
import com.casrusil.siierpai.modules.energy.domain.event.ChargingSessionCompletedEvent;
import com.casrusil.siierpai.modules.energy.domain.event.ChargingSessionStartedEvent;
import com.casrusil.siierpai.modules.energy.domain.event.EnergyConsumedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Listener para eventos del módulo Energy.
 * Genera asientos contables automáticos para sesiones de carga.
 * 
 * Flujo contable:
 * 1. Session Started: Crea cuenta por cobrar inicial (opcional)
 * 2. Energy Consumed: Registra consumo intermedio (opcional, para tracking
 * real-time)
 * 3. Session Completed: Reconoce ingreso y registra costo de energía
 * 
 * Asiento típico al completar sesión:
 * DR: Clientes Carga (1.1.05.002) - Total a cobrar
 * CR: Ingresos por Carga (4.1.02.001) - Reconocimiento de ingreso
 * DR: Costo de Energía (5.1.02.001) - Costo de la energía consumida
 * CR: Proveedores Energía (2.1.02.001) - Deuda con proveedor eléctrico
 */
@Component
public class ChargingEventListener {

        private static final Logger logger = LoggerFactory.getLogger(ChargingEventListener.class);

        private final AccountingEntryService accountingEntryService;
        private final com.casrusil.siierpai.modules.energy.application.service.pricing.DynamicPricingService dynamicPricingService;
        private final com.casrusil.siierpai.modules.accounting.infrastructure.persistence.repository.FailedChargingEventJpaRepository failedChargingEventRepository;

        // Configuración desde application.properties
        @Value("${ocpp.integration.accounting.revenue-account:4.1.02.001}")
        private String revenueAccount;

        @Value("${ocpp.integration.accounting.receivables-account:1.1.05.002}")
        private String receivablesAccount;

        @Value("${ocpp.integration.accounting.energy-cost-account:5.1.02.001}")
        private String energyCostAccount;

        // Precio por kWh (fallback si no hay pricing dinámico)
        @Value("${ocpp.pricing.price-per-kwh:150.0}")
        private BigDecimal pricePerKwh;

        // Costo de energía por kWh (TODO: obtener de contrato con proveedor)
        @Value("${ocpp.pricing.cost-per-kwh:80.0}")
        private BigDecimal costPerKwh;

        // CompanyId por defecto (TODO: obtener del contexto de seguridad)
        @Value("${app.default-company-id:00000000-0000-0000-0000-000000000000}")
        private UUID defaultCompanyId;

        public ChargingEventListener(
                        AccountingEntryService accountingEntryService,
                        com.casrusil.siierpai.modules.energy.application.service.pricing.DynamicPricingService dynamicPricingService,
                        com.casrusil.siierpai.modules.accounting.infrastructure.persistence.repository.FailedChargingEventJpaRepository failedChargingEventRepository) {
                this.accountingEntryService = accountingEntryService;
                this.dynamicPricingService = dynamicPricingService;
                this.failedChargingEventRepository = failedChargingEventRepository;
        }

        /**
         * Maneja el inicio de una sesión de carga.
         * Opcionalmente puede crear un asiento de cuenta por cobrar inicial.
         */
        @EventListener
        public void handleChargingSessionStarted(ChargingSessionStartedEvent event) {
                logger.info("Sesión de carga iniciada: sessionId={}, userId={}, stationId={}",
                                event.getSessionId(), event.getUserId(), event.getStationId());

                // Por ahora solo logueamos. El asiento se crea al completar la sesión.
                // En el futuro, aquí se podría crear un asiento de "Ingreso Diferido"
        }

        /**
         * Maneja el consumo de energía durante la sesión.
         * Útil para tracking en tiempo real, pero no genera asientos intermedios.
         */
        @EventListener
        public void handleEnergyConsumed(EnergyConsumedEvent event) {
                logger.debug("Energía consumida: sessionId={}, cumulative={} kWh",
                                event.getSessionId(), event.getCumulativeEnergyKwh());

                // Solo logging. Los asientos se generan al finalizar la sesión.
        }

        /**
         * Maneja la finalización de una sesión de carga de forma asíncrona.
         * Genera el asiento contable completo sin bloquear el flujo OCPP:
         * - Reconoce el ingreso por venta de energía
         * - Registra el costo de la energía consumida
         * - Crea la cuenta por cobrar al cliente
         * 
         * CRÍTICO: Procesamiento asíncrono con Virtual Threads para alta escalabilidad.
         */
        @Async
        @EventListener
        public void handleChargingSessionCompleted(ChargingSessionCompletedEvent event) {
                logger.info("⚡ Evento de carga recibido: sessionId={}, kWh={}, duration={}",
                                event.getSessionId(), event.getTotalEnergyKwh(), event.getSessionDuration());

                try {
                        createChargingAccountingEntry(event);
                        logger.info("✅ Asiento contable creado exitosamente para sesión {}", event.getSessionId());
                } catch (Exception e) {
                        logger.error("🔴 Error crítico al procesar contabilidad de carga {}: {}",
                                        event.getSessionId(), e.getMessage(), e);
                        // Dead Letter Queue: se persiste el evento fallido para reprocesarlo
                        // y no perder el ingreso a facturar.
                        persistFailedEvent(event, e.getMessage());
                }
        }

        /**
         * Persiste un evento de carga fallido en la Dead Letter Queue para su posterior
         * reproceso, evitando la perdida del ingreso asociado.
         */
        private void persistFailedEvent(ChargingSessionCompletedEvent event, String errorMessage) {
                try {
                        var failed = new com.casrusil.siierpai.modules.accounting.infrastructure.persistence.entity.FailedChargingEventEntity(
                                        event.getSessionId().getValue().toString(),
                                        event.getTotalEnergyKwh() != null ? event.getTotalEnergyKwh().toPlainString() : null,
                                        event.getUserId() != null ? event.getUserId().toString() : null,
                                        errorMessage);
                        failedChargingEventRepository.save(failed);
                        logger.warn("📥 Evento de carga {} enviado a la Dead Letter Queue para reproceso",
                                        event.getSessionId());
                } catch (Exception dlqError) {
                        logger.error("🔴 No se pudo persistir el evento fallido en la DLQ: {}", dlqError.getMessage(), dlqError);
                }
        }

        private void createChargingAccountingEntry(ChargingSessionCompletedEvent event) {
                AccountingEntryBuilder builder = new AccountingEntryBuilder();

                BigDecimal totalEnergyKwh = event.getTotalEnergyKwh();

                // Calculate revenue using dynamic pricing
                BigDecimal totalRevenue;
                try {
                        totalRevenue = dynamicPricingService.calculatePrice(
                                        event.getSessionId(),
                                        totalEnergyKwh,
                                        event.getOccurredOn(), // Changed from getCompletedAt() to getOccurredOn()
                                        event.getUserId());
                        logger.info("Dynamic pricing calculated: ${} for {} kWh", totalRevenue, totalEnergyKwh);
                } catch (Exception e) {
                        // Fallback to fixed pricing if dynamic pricing fails
                        totalRevenue = totalEnergyKwh.multiply(pricePerKwh);
                        logger.warn("Dynamic pricing failed, using fixed price: ${}/kWh", pricePerKwh, e);
                }

                BigDecimal totalCost = totalEnergyKwh.multiply(costPerKwh);

                // 1. DEBE: Clientes Carga (Cuenta por Cobrar)
                builder.addLine(
                                receivablesAccount,
                                "Clientes - Carga Eléctrica",
                                totalRevenue,
                                BigDecimal.ZERO);

                // 2. HABER: Ingresos por Carga Eléctrica
                builder.addLine(
                                revenueAccount,
                                "Ingresos por Carga Eléctrica",
                                BigDecimal.ZERO,
                                totalRevenue);

                // 3. DEBE: Costo de Energía (Gasto)
                builder.addLine(
                                energyCostAccount,
                                "Costo de Energía Eléctrica",
                                totalCost,
                                BigDecimal.ZERO);

                // 4. HABER: Proveedores de Energía (Pasivo)
                builder.addLine(
                                AccountConstants.PROVEEDORES_ENERGIA,
                                "Proveedores - Energía Eléctrica",
                                BigDecimal.ZERO,
                                totalCost);

                // Verificar que el asiento está balanceado
                if (!builder.isBalanced()) {
                        logger.error("Asiento contable desbalanceado para sesión: sessionId={}", event.getSessionId());
                        return;
                }

                // Crear el asiento contable
                AccountingEntry entry = new AccountingEntry(
                                CompanyId.of(defaultCompanyId),
                                LocalDate.now(), // Fecha contable = hoy
                                String.format("Carga Eléctrica - Sesión %s",
                                                event.getSessionId().getValue().toString().substring(0, 8)),
                                event.getSessionId().getValue().toString(), // Folio = SessionId
                                "CHARGING_SESSION",
                                event.getUserId().toString(), // RUT/ID del usuario
                                String.format("Usuario %s - %.2f kWh", event.getUserId().toString().substring(0, 8),
                                                totalEnergyKwh),
                                "ENERGY", // Tipo de documento
                                event.getSessionId().getValue().toString(), // Número de documento
                                "POSTED",
                                builder.build(),
                                EntryType.SALE // Es una venta de energía
                );

                accountingEntryService.recordEntry(entry);

                logger.info("Asiento contable creado exitosamente: sessionId={}, revenue={}, cost={}",
                                event.getSessionId(), totalRevenue, totalCost);
        }
}
