package com.casrusil.siierpai.modules.integration_sii.application.scheduler;

import com.casrusil.siierpai.modules.integration_sii.application.service.DteStatusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Job periódico que consulta el estado de los DTE enviados.
 *
 * <p>
 * Configurable: {@code sii.status.poll-enabled} (default true),
 * {@code sii.status.poll-interval-ms} (default 30 min) y
 * {@code sii.status.poll-initial-delay-ms} (default 1 min).
 * </p>
 */
@Component
@ConditionalOnProperty(prefix = "sii.status", name = "poll-enabled", havingValue = "true", matchIfMissing = true)
public class DteStatusScheduler {

    private static final Logger log = LoggerFactory.getLogger(DteStatusScheduler.class);

    private final DteStatusService dteStatusService;

    public DteStatusScheduler(DteStatusService dteStatusService) {
        this.dteStatusService = dteStatusService;
    }

    @Scheduled(fixedDelayString = "${sii.status.poll-interval-ms:1800000}", initialDelayString = "${sii.status.poll-initial-delay-ms:60000}")
    public void pollDteStatus() {
        try {
            int updated = dteStatusService.refreshPending();
            if (updated > 0) {
                log.info("Estado SII actualizado en {} DTE(s)", updated);
            }
        } catch (Exception e) {
            log.warn("Falló la consulta periódica de estados del SII: {}", e.getMessage());
        }
    }
}
