package com.casrusil.siierpai.modules.integration_sii.domain.service;

import com.casrusil.siierpai.modules.integration_sii.domain.exception.DteEmissionValidationException;
import com.casrusil.siierpai.modules.invoicing.domain.model.Invoice;
import com.casrusil.siierpai.modules.invoicing.domain.model.InvoiceType;
import com.casrusil.siierpai.modules.invoicing.domain.model.TransactionType;
import com.casrusil.siierpai.modules.sso.domain.model.Company;
import com.casrusil.siierpai.modules.sso.domain.port.out.CompanyRepository;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DteEmissionValidatorTest {

    private final CompanyRepository companyRepository = Mockito.mock(CompanyRepository.class);
    private final DteEmissionValidator validator = new DteEmissionValidator(companyRepository);

    @Test
    void shouldRejectWhenCompanyLacksGiroAndActeco() {
        CompanyId companyId = CompanyId.random();
        Company company = Mockito.mock(Company.class);
        Mockito.when(company.getRazonSocial()).thenReturn("Emisor SpA");
        Mockito.when(company.getGiro()).thenReturn(null);
        Mockito.when(company.getActeco()).thenReturn(" ");
        Mockito.when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));

        DteEmissionValidationException ex = assertThrows(DteEmissionValidationException.class,
                () -> validator.validate(invoice(companyId, true)));

        assertTrue(ex.getMissingFields().contains("giro del emisor"));
        assertTrue(ex.getMissingFields().contains("acteco del emisor"));
    }

    @Test
    void shouldRejectWhenReceiverNameIsMissing() {
        CompanyId companyId = CompanyId.random();
        Company company = completeCompany();
        Mockito.when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));

        DteEmissionValidationException ex = assertThrows(DteEmissionValidationException.class,
                () -> validator.validate(invoice(companyId, false)));

        assertTrue(ex.getMissingFields().contains("razón social del receptor"));
    }

    @Test
    void shouldPassWithCompleteData() {
        CompanyId companyId = CompanyId.random();
        Company company = completeCompany();
        Mockito.when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));

        assertDoesNotThrow(() -> validator.validate(invoice(companyId, true)));
    }

    private Company completeCompany() {
        Company company = Mockito.mock(Company.class);
        Mockito.when(company.getRazonSocial()).thenReturn("Emisor SpA");
        Mockito.when(company.getGiro()).thenReturn("Servicios");
        Mockito.when(company.getActeco()).thenReturn("620100");
        Mockito.when(company.getCommercialAddress()).thenReturn("Av. 1");
        Mockito.when(company.getComuna()).thenReturn("Santiago");
        return company;
    }

    private Invoice invoice(CompanyId companyId, boolean withReceiver) {
        Invoice invoice = Invoice.create(
                companyId,
                InvoiceType.FACTURA_ELECTRONICA,
                10L,
                "76123456-7",
                "76987654-3",
                "Emisor SpA",
                LocalDate.now(),
                new BigDecimal("1000"),
                new BigDecimal("190"),
                new BigDecimal("1190"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                Invoice.ORIGIN_MANUAL,
                TransactionType.SALE,
                Collections.emptyList());
        return withReceiver ? invoice.withReceiverName("Receptor SpA") : invoice;
    }
}
