package com.casrusil.siierpai.modules.fees.infrastructure.adapter.in.rest;

import com.casrusil.siierpai.modules.fees.application.service.FeeReceiptService;
import com.casrusil.siierpai.modules.fees.domain.model.FeeReceipt;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import com.casrusil.siierpai.shared.infrastructure.context.CompanyContext;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class FeeQueryControllerTest {

    @Test
    void listFees_returnsFrontContract() throws Exception {
        UUID id = UUID.randomUUID();
        CompanyId companyId = new CompanyId(UUID.randomUUID());
        FeeReceipt receipt = new FeeReceipt(
                id,
                companyId,
                123L,
                "11111111-1",
                "22222222-2",
                "Prestador de prueba",
                LocalDate.of(2026, 1, 31),
                new BigDecimal("1000000.00"),
                new BigDecimal("152500.00"),
                new BigDecimal("847500.00"),
                FeeReceipt.Status.VALID);

        FeeReceiptService service = mock(FeeReceiptService.class);
        when(service.listFees(any(CompanyId.class), eq(2026))).thenReturn(List.of(receipt));

        MockMvc mockMvc = MockMvcBuilders
                .standaloneSetup(new FeeQueryController(service))
                .build();

        ScopedValue.where(CompanyContext.COMPANY_ID, companyId).run(() -> {
            try {
                mockMvc.perform(get("/api/v1/fees").param("year", "2026"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$[0].id").value(id.toString()))
                        .andExpect(jsonPath("$[0].folio").value(123))
                        .andExpect(jsonPath("$[0].issuerRut").value("11111111-1"))
                        .andExpect(jsonPath("$[0].issuerName").value("Prestador de prueba"))
                        .andExpect(jsonPath("$[0].issueDate").value("2026-01-31"))
                        .andExpect(jsonPath("$[0].grossAmount").value(1000000.00))
                        .andExpect(jsonPath("$[0].retentionAmount").value(152500.00))
                        .andExpect(jsonPath("$[0].netAmount").value(847500.00))
                        .andExpect(jsonPath("$[0].status").value("VALID"));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}
