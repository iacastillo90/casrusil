package com.casrusil.siierpai.modules.payroll.infrastructure.web;

import com.casrusil.siierpai.modules.payroll.application.service.PayrollUseCase;
import com.casrusil.siierpai.modules.payroll.domain.dto.PayrollProcessRequest;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Contrato HTTP del archivo Previred: debe declarar el charset ANSI en el
 * Content-Type para que el navegador no lo reinterprete como UTF-8.
 */
class PayrollPreviredEndpointTest {

    @Test
    void exportDeclaresAnsiCharsetAndAttachment() throws Exception {
        PayrollUseCase useCase = mock(PayrollUseCase.class);
        when(useCase.generatePreviredFile(any(PayrollProcessRequest.class), anyString()))
                .thenReturn("María".getBytes(StandardCharsets.ISO_8859_1));
        PayrollController controller = new PayrollController(useCase);

        var company = com.casrusil.siierpai.shared.domain.valueobject.CompanyId.random();
        var response = java.lang.ScopedValue
                .where(com.casrusil.siierpai.shared.infrastructure.context.CompanyContext.COMPANY_ID, company)
                .call(() -> controller.exportPreviredFile(
                        PayrollProcessRequest.builder().year(2026).month(11).build()));

        // La empresa sale del contexto (JWT), no de un header.
        org.mockito.Mockito.verify(useCase).generatePreviredFile(any(PayrollProcessRequest.class),
                org.mockito.ArgumentMatchers.eq(company.value().toString()));

        assertEquals(200, response.getStatusCode().value());
        assertEquals("text/plain;charset=windows-1252", response.getHeaders().getContentType().toString());
        assertTrue(response.getHeaders().getFirst("Content-Disposition").contains("previred_2026_11.txt"));
    }
}
