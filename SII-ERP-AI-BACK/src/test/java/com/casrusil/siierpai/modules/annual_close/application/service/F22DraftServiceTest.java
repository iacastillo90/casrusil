package com.casrusil.siierpai.modules.annual_close.application.service;

import com.casrusil.siierpai.modules.accounting.domain.service.BalanceSheetService;
import com.casrusil.siierpai.modules.accounting.domain.service.F29CalculatorService;
import com.casrusil.siierpai.modules.annual_close.domain.port.CompanyTaxProfileRepositoryPort;
import com.casrusil.siierpai.shared.domain.exception.UnprocessableEntityException;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class F22DraftServiceTest {

    @Test
    void missingTaxProfileShouldReturnActionable422Exception() {
        CompanyTaxProfileRepositoryPort profiles = mock(CompanyTaxProfileRepositoryPort.class);
        when(profiles.findByCompanyId(anyString())).thenReturn(Optional.empty());
        F22DraftService service = new F22DraftService(
                profiles,
                mock(RLICalculatorService.class),
                mock(IDPCCalculatorService.class),
                mock(BalanceSheetService.class),
                mock(F29CalculatorService.class));

        UnprocessableEntityException ex = assertThrows(UnprocessableEntityException.class,
                () -> service.calculateAnnualTaxes(2025, "company-1"));

        assertTrue(ex.getMessage().contains("perfil tributario"));
        assertTrue(ex.getMessage().contains("Configuración > Empresa > Perfil tributario"));
        assertTrue(ex.getMessage().contains("company-1"));
    }
}
