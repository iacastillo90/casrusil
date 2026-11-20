package com.casrusil.siierpai.modules.integration_sii.application.service;

import com.casrusil.siierpai.modules.integration_sii.domain.model.SiiDteQueryResult;
import com.casrusil.siierpai.modules.integration_sii.domain.model.SiiToken;
import com.casrusil.siierpai.modules.integration_sii.domain.port.out.SiiDteStatusClient;
import com.casrusil.siierpai.modules.integration_sii.domain.port.out.SiiTokenRepository;
import com.casrusil.siierpai.modules.integration_sii.domain.service.SiiDteStatusMapper;
import com.casrusil.siierpai.modules.integration_sii.domain.service.SiiDteStatusMapper.SiiDocumentStatus;
import com.casrusil.siierpai.modules.invoicing.application.service.InvoiceAuditTrailService;
import com.casrusil.siierpai.modules.invoicing.domain.event.InvoiceSiiAcceptedEvent;
import com.casrusil.siierpai.modules.invoicing.domain.model.Invoice;
import com.casrusil.siierpai.modules.invoicing.domain.model.InvoiceAuditLog;
import com.casrusil.siierpai.modules.invoicing.domain.port.out.InvoiceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;



/**
 * Consulta el estado de los DTE enviados y actualiza la factura.
 *
 * <p>
 * Flujo: {@code getEstUp} por TrackID; si el envío está procesado se consulta
 * {@code getEstDte} para el estado a nivel de documento. El estado resultante
 * se persiste en {@code dte_status}, se registra en {@code invoice_audit_log} y,
 * cuando el SII acepta, se publica {@link InvoiceSiiAcceptedEvent} (que dispara
 * el asiento contable).
 * </p>
 *
 * <p>
 * Idempotencia: sólo se consultan facturas en estado ENVIADO; una vez ACEPTADO
 * o RECHAZADO dejan de seleccionarse. Los reintentos se cuentan en memoria
 * (se reinician al reiniciar la aplicación); persistirlos requiere una columna
 * nueva, documentada como pendiente.
 * </p>
 */
@Service
public class DteStatusService {

    private static final Logger log = LoggerFactory.getLogger(DteStatusService.class);

    private final InvoiceRepository invoiceRepository;
    private final InvoiceAuditTrailService auditTrailService;
    private final ApplicationEventPublisher eventPublisher;
    private final SiiDteStatusClient statusClient;
    private final SiiTokenRepository tokenRepository;
    private final SiiDteStatusMapper mapper;
    private final int maxAttempts;
    private final String envioProcesadoCode;

    public DteStatusService(
            InvoiceRepository invoiceRepository,
            InvoiceAuditTrailService auditTrailService,
            ApplicationEventPublisher eventPublisher,
            SiiDteStatusClient statusClient,
            SiiTokenRepository tokenRepository,
            SiiDteStatusMapper mapper,
            @Value("${sii.status.max-attempts:5}") int maxAttempts,
            @Value("${sii.estado.envio-procesado:EPR}") String envioProcesadoCode) {
        this.invoiceRepository = invoiceRepository;
        this.auditTrailService = auditTrailService;
        this.eventPublisher = eventPublisher;
        this.statusClient = statusClient;
        this.tokenRepository = tokenRepository;
        this.mapper = mapper;
        this.maxAttempts = maxAttempts;
        this.envioProcesadoCode = envioProcesadoCode;
    }

    /**
     * Consulta todas las facturas en estado ENVIADO.
     *
     * @return cantidad de facturas cuyo estado cambió
     */
    public int refreshPending() {
        int updated = 0;
        for (Invoice invoice : invoiceRepository.findByDteStatus(Invoice.DTE_STATUS_SENT)) {
            try {
                if (refresh(invoice)) {
                    updated++;
                }
            } catch (Exception e) {
                log.warn("No se pudo consultar el estado del DTE {}: {}", invoice.getFolio(), e.getMessage());
            }
        }
        return updated;
    }

    @Transactional
    public boolean refresh(Invoice invoice) {
        if (!Invoice.DTE_STATUS_SENT.equals(invoice.getDteStatus()) || invoice.getSiiTrackId() == null) {
            return false;
        }

        if (invoice.getSiiStatusAttempts() >= maxAttempts) {
            log.warn("DTE {} alcanzó el máximo de reintentos de consulta ({})", invoice.getFolio(), maxAttempts);
            return false;
        }
        invoice.registerSiiStatusAttempt();
        invoiceRepository.save(invoice);

        SiiToken token = tokenRepository.findByCompanyId(invoice.getCompanyId())
                .filter(SiiToken::isValid)
                .orElse(null);
        if (token == null) {
            log.warn("Sin token SII vigente para la empresa {}; no se consulta el DTE {}",
                    invoice.getCompanyId().value(), invoice.getFolio());
            return false;
        }

        String[] emisor = splitRut(invoice.getIssuerRut());
        SiiDteQueryResult envio = statusClient.queryEnvio(token.token(), emisor[0], emisor[1],
                invoice.getSiiTrackId());

        SiiDocumentStatus status = mapper.map(envio);
        if (status == SiiDocumentStatus.PENDIENTE && isEnvioProcesado(envio)) {
            String[] receptor = splitRut(invoice.getReceiverRut());
            SiiDteQueryResult documento = statusClient.queryDte(token.token(),
                    emisor[0], emisor[1], emisor[0], emisor[1], receptor[0], receptor[1],
                    invoice.getType().getCode(), invoice.getFolio(), invoice.getDate(),
                    invoice.getTotalAmount());
            status = mapper.map(documento);
        }

        return apply(invoice, status, envio);
    }

    private boolean apply(Invoice invoice, SiiDocumentStatus status, SiiDteQueryResult result) {
        if (status == SiiDocumentStatus.PENDIENTE) {
            return false;
        }

        String previous = invoice.getDteStatus();
        invoice.applySiiStatus(status.name(), invoice.getSiiTrackId(), invoice.getSentAt());
        invoiceRepository.save(invoice);

        auditTrailService.record(invoice.getCompanyId(), invoice.getId(), InvoiceAuditLog.ACTION_STATUS_UPDATED,
                "Estado SII " + status + " (" + result.estado() + " " + safe(result.glosa()) + ")");

        boolean accepted = status == SiiDocumentStatus.ACEPTADO
                || status == SiiDocumentStatus.ACEPTADO_CON_REPAROS;
        boolean wasAccepted = Invoice.DTE_STATUS_SENT.equals(previous) || "PENDIENTE".equals(previous);
        if (accepted && wasAccepted) {
            eventPublisher.publishEvent(new InvoiceSiiAcceptedEvent(invoice));
        }
        return true;
    }

    private boolean isEnvioProcesado(SiiDteQueryResult result) {
        return result.estado() != null
                && result.estado().trim().equalsIgnoreCase(envioProcesadoCode);
    }

    /**
     * Separa RUT y dígito verificador. El SII los recibe en campos distintos.
     *
     * @param rut RUT con formato 12345678-9
     * @return [cuerpo, dv]; si no hay guión, dv vacío
     */
    static String[] splitRut(String rut) {
        if (rut == null) {
            return new String[] { "", "" };
        }
        String clean = rut.replace(".", "").trim();
        int idx = clean.lastIndexOf('-');
        if (idx < 0) {
            return new String[] { clean, "" };
        }
        return new String[] { clean.substring(0, idx), clean.substring(idx + 1) };
    }

    private static String safe(String value) {
        return value != null ? value : "";
    }
}
