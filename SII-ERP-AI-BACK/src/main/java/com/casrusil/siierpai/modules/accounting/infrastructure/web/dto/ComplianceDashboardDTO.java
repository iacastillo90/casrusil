package com.casrusil.siierpai.modules.accounting.infrastructure.web.dto;

import com.casrusil.siierpai.modules.accounting.domain.dto.SiiAuditReportDTO;
import com.casrusil.siierpai.modules.accounting.domain.model.AnomalyWarning;
import com.casrusil.siierpai.modules.accounting.domain.model.F29Report;
import com.casrusil.siierpai.modules.accounting.domain.model.TaxAuditReport;
import com.casrusil.siierpai.modules.sustainability.infrastructure.adapter.in.rest.dto.GreenPassportDTO;
import com.casrusil.siierpai.modules.banking.infrastructure.web.dto.ReconciliationStatsDTO;

import java.util.List;

/**
 * 🧠 FERRARI ENGINE PHASE 4: Dashboard unificado de compliance e insights.
 * 
 * <p>
 * Integra todas las métricas del sistema en un solo endpoint:
 * <ul>
 * <li>Estado de conciliación SII</li>
 * <li>Predicción F29</li>
 * <li>Salud contable interna</li>
 * <li>Alertas de IA</li>
 * <li>Green Passport (sostenibilidad)</li>
 * <li>Estadísticas de conciliación bancaria</li>
 * <li>Score global de salud financiera</li>
 * </ul>
 * 
 * @param siiStatus           Estado conciliación SII (Faltan/Sobran DTEs)
 * @param f29Preview          Predicción impuestos a pagar
 * @param accountingStatus    Salud contable interna
 * @param aiAlerts            Alertas de Inteligencia Artificial
 * @param greenPassport       Métricas de sostenibilidad
 * @param reconciliationStats Estadísticas de conciliación bancaria
 * @param globalHealthScore   Puntaje 0-100 de salud financiera
 * @since 2.0 - Ferrari Engine Phase 4
 */
public record ComplianceDashboardDTO(
                SiiAuditReportDTO siiStatus,
                F29Report f29Preview,
                TaxAuditReport accountingStatus,
                List<AnomalyWarning> aiAlerts,
                GreenPassportDTO greenPassport,
                ReconciliationStatsDTO reconciliationStats,
                int globalHealthScore) {
}
