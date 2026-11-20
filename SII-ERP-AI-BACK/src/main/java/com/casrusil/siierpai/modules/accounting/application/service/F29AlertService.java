package com.casrusil.siierpai.modules.accounting.application.service;

import com.casrusil.siierpai.modules.accounting.domain.service.PeriodClosingService;
import com.casrusil.siierpai.modules.accounting.infrastructure.persistence.entity.F29AlertEntity;
import com.casrusil.siierpai.modules.accounting.infrastructure.persistence.repository.F29AlertJpaRepository;
import com.casrusil.siierpai.modules.invoicing.domain.model.Invoice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

/**
 * Registra alertas cuando un asiento de venta cae en un período ya cerrado o
 * declarado.
 *
 * <p>
 * No reabre el período ni modifica el F29: deja una alerta consultable para que
 * el contribuyente evalúe una declaración rectificatoria.
 * </p>
 */
@Service
public class F29AlertService {

    private static final String REASON_PERIOD_CLOSED = "PERIODO_CERRADO_O_DECLARADO";

    private final F29AlertJpaRepository alertRepository;
    private final PeriodClosingService periodClosingService;

    public F29AlertService(F29AlertJpaRepository alertRepository, PeriodClosingService periodClosingService) {
        this.alertRepository = alertRepository;
        this.periodClosingService = periodClosingService;
    }

    /**
     * Si el período de la factura está cerrado, deja una alerta idempotente.
     *
     * @param invoice factura de venta contabilizada
     * @return true si se registró una alerta nueva
     */
    @Transactional
    public boolean registerIfPeriodClosed(Invoice invoice) {
        YearMonth period = YearMonth.from(invoice.getDate());
        if (!periodClosingService.isPeriodClosed(invoice.getCompanyId(), period)) {
            return false;
        }
        UUID companyId = invoice.getCompanyId().value();
        if (alertRepository.existsByCompanyIdAndPeriodYearAndPeriodMonthAndInvoiceFolio(
                companyId, period.getYear(), period.getMonthValue(), invoice.getFolio())) {
            return false;
        }

        F29AlertEntity alert = new F29AlertEntity(
                UUID.randomUUID(),
                companyId,
                period.getYear(),
                period.getMonthValue(),
                invoice.getFolio(),
                invoice.getTaxAmount(),
                REASON_PERIOD_CLOSED,
                Instant.now(),
                false);
        alertRepository.save(alert);
        return true;
    }

    /**
     * Alertas de F29 desactualizado de una empresa, más recientes primero.
     *
     * @param companyId empresa
     * @return alertas registradas
     */
    public List<F29AlertEntity> listAlerts(UUID companyId) {
        return alertRepository.findByCompanyIdOrderByCreatedAtDesc(companyId);
    }
}
