package com.casrusil.siierpai.modules.accounting.infrastructure.web.dto;

import com.casrusil.siierpai.modules.accounting.domain.dto.SiiAuditReportDTO;
import com.casrusil.siierpai.modules.accounting.domain.model.AnomalyWarning;
import com.casrusil.siierpai.modules.accounting.domain.model.F29Report;
import com.casrusil.siierpai.modules.accounting.domain.model.TaxAuditReport;
import com.casrusil.siierpai.modules.compliance.infrastructure.persistence.entity.TaxParameterEntity;
import com.casrusil.siierpai.modules.sustainability.infrastructure.adapter.in.rest.dto.GreenPassportDTO;
// ReconciliationStatsDTO moved from banking module to avoid entity conflicts
import com.casrusil.siierpai.modules.accounting.infrastructure.web.dto.ReconciliationStatsDTO;

import java.util.List;

/**
 * Dashboard unificado de compliance e insights.
 */
public record ComplianceDashboardDTO(
        SiiAuditReportDTO siiStatus,
        F29Report f29Preview,
        TaxAuditReport accountingStatus,
        List<AnomalyWarning> aiAlerts,
        GreenPassportDTO greenPassport,
        ReconciliationStatsDTO reconciliationStats,
        int globalHealthScore,
        List<AnomalyWarning> acuseReciboAlerts,
        TaxParameterEntity taxParameters) {
}