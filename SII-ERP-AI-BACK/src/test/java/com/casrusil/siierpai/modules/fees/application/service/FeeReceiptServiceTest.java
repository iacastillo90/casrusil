package com.casrusil.siierpai.modules.fees.application.service;

import com.casrusil.siierpai.modules.fees.domain.model.FeeReceipt;
import com.casrusil.siierpai.modules.fees.domain.port.out.FeeReceiptRepository;
import com.casrusil.siierpai.modules.sso.domain.port.out.CompanyRepository;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class FeeReceiptServiceTest {

    private final StubFeeReceiptRepository repository = new StubFeeReceiptRepository();
    private final FeeReceiptService service = new FeeReceiptService(
            repository,
            mock(FeeReceiptParser.class),
            mock(CompanyRepository.class));

    @Test
    void listFees_withoutYear_returnsAllOrderedByDateAndFolioDesc() {
        CompanyId companyId = new CompanyId(UUID.randomUUID());
        repository.all = List.of(
                receipt(companyId, 1L, LocalDate.of(2025, 12, 31)),
                receipt(companyId, 2L, LocalDate.of(2026, 1, 31)),
                receipt(companyId, 3L, LocalDate.of(2026, 1, 31)));

        List<FeeReceipt> result = service.listFees(companyId, null);

        assertEquals(List.of(3L, 2L, 1L), result.stream().map(FeeReceipt::getFolio).toList());
    }

    @Test
    void listFees_withYear_filtersByIssueYear() {
        CompanyId companyId = new CompanyId(UUID.randomUUID());
        repository.byYear = List.of(receipt(companyId, 7L, LocalDate.of(2026, 5, 10)));

        List<FeeReceipt> result = service.listFees(companyId, 2026);

        assertEquals(1, result.size());
        assertEquals(7L, result.get(0).getFolio());
        assertEquals(2026, repository.requestedYear);
    }

    private static FeeReceipt receipt(CompanyId companyId, Long folio, LocalDate issueDate) {
        return new FeeReceipt(
                UUID.randomUUID(),
                companyId,
                folio,
                "11111111-1",
                "22222222-2",
                "Prestador de prueba",
                issueDate,
                new BigDecimal("1000000.00"),
                new BigDecimal("152500.00"),
                new BigDecimal("847500.00"),
                FeeReceipt.Status.VALID);
    }

    static class StubFeeReceiptRepository implements FeeReceiptRepository {
        List<FeeReceipt> all = new ArrayList<>();
        List<FeeReceipt> byYear = new ArrayList<>();
        Integer requestedYear;

        @Override
        public void save(FeeReceipt feeReceipt) {
            all.add(feeReceipt);
        }

        @Override
        public void saveAll(List<FeeReceipt> feeReceipts) {
            all.addAll(feeReceipts);
        }

        @Override
        public List<FeeReceipt> findByCompanyId(CompanyId companyId) {
            return all;
        }

        @Override
        public List<FeeReceipt> findByCompanyIdAndIssueDateBetween(CompanyId companyId, LocalDate startDate,
                LocalDate endDate) {
            return all;
        }

        @Override
        public List<FeeReceipt> findByCompanyIdAndYear(CompanyId companyId, int year) {
            requestedYear = year;
            return byYear;
        }

        @Override
        public Optional<FeeReceipt> findByCompanyIdAndFolioAndIssuerRut(CompanyId companyId, Long folio,
                String issuerRut) {
            return Optional.empty();
        }
    }
}
