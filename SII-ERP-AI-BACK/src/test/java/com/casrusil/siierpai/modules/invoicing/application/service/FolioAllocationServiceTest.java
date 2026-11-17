package com.casrusil.siierpai.modules.invoicing.application.service;

import com.casrusil.siierpai.modules.integration_sii.domain.model.CafFolioWindow;
import com.casrusil.siierpai.modules.integration_sii.domain.port.out.CafRepository;
import com.casrusil.siierpai.modules.invoicing.domain.port.out.InvoiceRepository;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FolioAllocationServiceTest {

    private CafRepository cafRepository;
    private InvoiceRepository invoiceRepository;
    private FolioAllocationService service;
    private CompanyId companyId;

    @BeforeEach
    void setUp() {
        cafRepository = mock(CafRepository.class);
        invoiceRepository = mock(InvoiceRepository.class);
        service = new FolioAllocationService(cafRepository, invoiceRepository);
        companyId = CompanyId.random();
    }

    @Test
    void shouldReserveNextFolioAfterMaxUsedInWindow() {
        UUID cafId = UUID.randomUUID();
        CafFolioWindow window = new CafFolioWindow(cafId, 100L, 200L, 150L);
        when(cafRepository.lockAvailableWindows(companyId, "33")).thenReturn(List.of(window));
        when(invoiceRepository.findMaxFolioInRange(companyId, 33, 100L, 200L)).thenReturn(Optional.of(160L));

        Optional<Long> folio = service.reserveNextFolio(companyId, 33);

        assertEquals(Optional.of(161L), folio);
        verify(cafRepository).advanceCurrentFolio(cafId, 162L);
    }

    @Test
    void shouldNotReuseFoliosBelowCurrentPointer() {
        UUID cafId = UUID.randomUUID();
        CafFolioWindow window = new CafFolioWindow(cafId, 100L, 200L, 170L);
        when(cafRepository.lockAvailableWindows(companyId, "33")).thenReturn(List.of(window));
        when(invoiceRepository.findMaxFolioInRange(companyId, 33, 100L, 200L)).thenReturn(Optional.of(160L));

        Optional<Long> folio = service.reserveNextFolio(companyId, 33);

        assertEquals(Optional.of(170L), folio);
        verify(cafRepository).advanceCurrentFolio(cafId, 171L);
    }

    @Test
    void shouldSkipExhaustedWindowAndUseNextOne() {
        UUID exhausted = UUID.randomUUID();
        UUID available = UUID.randomUUID();
        when(cafRepository.lockAvailableWindows(companyId, "33")).thenReturn(List.of(
                new CafFolioWindow(exhausted, 1L, 10L, 11L),
                new CafFolioWindow(available, 20L, 30L, 20L)));
        when(invoiceRepository.findMaxFolioInRange(eq(companyId), anyInt(), anyLong(), anyLong()))
                .thenReturn(Optional.empty());

        Optional<Long> folio = service.reserveNextFolio(companyId, 33);

        assertEquals(Optional.of(20L), folio);
        verify(cafRepository).advanceCurrentFolio(exhausted, 11L);
        verify(cafRepository).advanceCurrentFolio(available, 21L);
    }

    @Test
    void shouldReturnEmptyWhenNoWindowsAvailable() {
        when(cafRepository.lockAvailableWindows(companyId, "33")).thenReturn(List.of());

        Optional<Long> folio = service.reserveNextFolio(companyId, 33);

        assertTrue(folio.isEmpty());
    }
}
