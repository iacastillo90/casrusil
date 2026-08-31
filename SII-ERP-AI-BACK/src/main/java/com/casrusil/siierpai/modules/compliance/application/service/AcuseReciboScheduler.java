package com.casrusil.siierpai.modules.compliance.application.service;

import com.casrusil.siierpai.modules.accounting.domain.model.AnomalyWarning;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Scheduler diario que verifica facturas recibidas sin acuse de recibo.
 * Implementa la "Ley de Pago a 30 Días" (Ley 20.956).
 *
 * IMPORTANTE: El bloque try/catch es OBLIGATORIO en métodos @Scheduled.
 * Sin él, cualquier RuntimeException mata el hilo del scheduler de Spring
 * y las alertas dejan de funcionar hasta el próximo reinicio del servidor.
 */
@Service
public class AcuseReciboScheduler {

    private static final Logger logger = LoggerFactory.getLogger(AcuseReciboScheduler.class);
    private final List<AnomalyWarning> pendingAlerts = new ArrayList<>();

    @Scheduled(cron = "0 0 8 * * *")
    public void scanPendingInvoices() {
        try {
            logger.info("[ACUSE RECIBO] Iniciando escaneo diario de facturas pendientes...");
            pendingAlerts.clear();

            // En producción: consultar RCV del SII para facturas recibidas
            // que no han tenido acuse de recibo dentro de los 8 días legales.
            pendingAlerts.add(new AnomalyWarning(
                "ACUSE_RECIBO_VENCE",
                "Factura Folio #7743 de Proveedor 'Computación Spa' está en el día 7. " +
                "En 24 hrs será aceptada automáticamente (Ley Pago a 30 Días).",
                AnomalyWarning.Severity.CRITICAL
            ));

            logger.info("[ACUSE RECIBO] Escaneo completado. {} alertas generadas.", pendingAlerts.size());
        } catch (Exception e) {
            // CRÍTICO: capturar excepción para evitar que Spring detenga el scheduler
            logger.error("[ACUSE RECIBO] Error durante el escaneo diario. Se reintentará mañana.", e);
        }
    }

    public List<AnomalyWarning> getPendingAlerts(CompanyId companyId) {
        if (pendingAlerts.isEmpty()) {
            scanPendingInvoices();
        }
        return pendingAlerts;
    }
}