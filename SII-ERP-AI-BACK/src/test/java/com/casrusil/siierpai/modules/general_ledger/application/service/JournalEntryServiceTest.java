package com.casrusil.siierpai.modules.general_ledger.application.service;

import com.casrusil.siierpai.shared.domain.event.PayrollCalculatedEvent;
import com.casrusil.siierpai.shared.domain.event.StockMovedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * Pruebas unitarias para el servicio de Libro Mayor.
 * Verifica la correcta escucha de los eventos de dominio y la asimilación contable.
 */
@ExtendWith(MockitoExtension.class)
class JournalEntryServiceTest {

    @InjectMocks
    private JournalEntryService journalEntryService;

    @BeforeEach
    void setUp() {
    }

    @Test
    void testHandleStockMovedEvent_ShouldProcessWithoutErrors() {
        // Arrange
        StockMovedEvent event = new StockMovedEvent(
                "comp-1",
                1L,
                "IN",
                new BigDecimal("10"),
                new BigDecimal("1000"),
                new BigDecimal("10000"),
                LocalDateTime.now()
        );

        // Act & Assert
        // Por ahora verificamos que el listener procese el evento sin lanzar excepciones
        assertDoesNotThrow(() -> journalEntryService.handleStockMovedEvent(event));
    }

    @Test
    void testHandlePayrollCalculatedEvent_ShouldProcessWithoutErrors() {
        // Arrange
        PayrollCalculatedEvent event = new PayrollCalculatedEvent(
                "comp-1",
                YearMonth.of(2026, 8),
                Collections.emptyMap(),
                new BigDecimal("800000"),
                new BigDecimal("1000000")
        );

        // Act & Assert
        assertDoesNotThrow(() -> journalEntryService.handlePayrollCalculatedEvent(event));
    }
}
