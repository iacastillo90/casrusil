package com.casrusil.siierpai.modules.fees.infrastructure.adapter.in.rest;

import com.casrusil.siierpai.modules.fees.application.service.FeeReceiptService;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import com.casrusil.siierpai.shared.infrastructure.context.CompanyContext;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FeeImportControllerTest {

    private final FeeReceiptService service = mock(FeeReceiptService.class);
    private final FeeImportController controller = new FeeImportController(service);

    @Test
    void importShouldUseCompanyContextInsteadOfRequestParam() throws Exception {
        CompanyId companyId = CompanyId.random();
        MockMultipartFile file = new MockMultipartFile("file", "fees.csv", "text/csv", "a;b".getBytes());
        when(service.importFromCsv(any(), eq(companyId)))
                .thenReturn(new FeeReceiptService.ImportResult(1, 0, "ok"));

        var response = ScopedValue.where(CompanyContext.COMPANY_ID, companyId)
                .call(() -> controller.importFees(file));

        assertEquals(200, response.getStatusCode().value());
        verify(service).importFromCsv(any(), eq(companyId));
    }
}
