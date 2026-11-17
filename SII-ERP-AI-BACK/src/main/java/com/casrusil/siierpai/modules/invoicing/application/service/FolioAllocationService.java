package com.casrusil.siierpai.modules.invoicing.application.service;

import com.casrusil.siierpai.modules.integration_sii.domain.model.CafFolioWindow;
import com.casrusil.siierpai.modules.integration_sii.domain.port.out.CafRepository;
import com.casrusil.siierpai.modules.invoicing.domain.port.out.FolioSource;
import com.casrusil.siierpai.modules.invoicing.domain.port.out.InvoiceRepository;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Asigna folios desde los CAF disponibles de forma atómica.
 *
 * <p>
 * Se apoya en el bloqueo pesimista ({@code PESSIMISTIC_WRITE}) que aplica
 * {@link CafRepository#lockAvailableWindows}. Dicho bloqueo se mantiene hasta
 * que la transacción del llamador hace commit, por lo que la reserva del folio
 * y la persistencia de la factura ocurren como una sola operación atómica.
 * Esto garantiza que dos peticiones concurrentes nunca reciban el mismo folio.
 * </p>
 *
 * @since 1.0
 */
@Service
public class FolioAllocationService implements FolioSource {

    private final CafRepository cafRepository;
    private final InvoiceRepository invoiceRepository;

    public FolioAllocationService(CafRepository cafRepository, InvoiceRepository invoiceRepository) {
        this.cafRepository = cafRepository;
        this.invoiceRepository = invoiceRepository;
    }

    @Override
    @Transactional
    public Optional<Long> reserveNextFolio(CompanyId companyId, int tipoDte) {
        List<CafFolioWindow> windows = cafRepository.lockAvailableWindows(companyId, String.valueOf(tipoDte));
        if (windows.isEmpty()) {
            return Optional.empty();
        }

        for (CafFolioWindow window : windows) {
            long maxUsed = invoiceRepository
                    .findMaxFolioInRange(companyId, tipoDte, window.initialFolio(), window.finalFolio())
                    .orElse(window.initialFolio() - 1);
            long next = Math.max(window.currentFolio(), maxUsed + 1);

            if (next > window.finalFolio()) {
                cafRepository.advanceCurrentFolio(window.cafId(), window.finalFolio() + 1);
                continue;
            }

            cafRepository.advanceCurrentFolio(window.cafId(), next + 1);
            return Optional.of(next);
        }

        return Optional.empty();
    }
}
