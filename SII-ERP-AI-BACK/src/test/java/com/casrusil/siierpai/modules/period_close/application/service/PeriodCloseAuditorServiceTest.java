package com.casrusil.siierpai.modules.period_close.application.service;

import com.casrusil.siierpai.modules.period_close.domain.dto.PeriodAuditResponse;
import com.casrusil.siierpai.modules.period_close.domain.dto.PeriodCloseRequest;
import com.casrusil.siierpai.modules.period_close.domain.model.PeriodAuditLog;
import com.casrusil.siierpai.modules.period_close.domain.port.PeriodAuditLogRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.YearMonth;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PeriodCloseAuditorServiceTest {

    @Mock
    private PeriodAuditLogRepositoryPort auditLogRepository;

    @Mock
    private AiIntegrationService aiIntegrationService;

    @Mock
    private DepreciationServiceImpl depreciationService;

    @InjectMocks
    private PeriodCloseAuditorService auditorService;

    private static final String COMPANY_ID = "empresa-123";

    @Test
    void auditPeriod_ShouldLogFindingsAndReturnAiRecommendation() {
        // Arrange
        PeriodCloseRequest request = PeriodCloseRequest.builder().year(2026).month(11).build();
        YearMonth period = YearMonth.of(2026, 11);

        when(depreciationService.calculateAndProvisionDepreciation(period, COMPANY_ID))
                .thenReturn(Collections.singletonList("Depreciation simulated"));

        String fakeAiResponse = "AI Analysis: Some discrepancies found.";
        when(aiIntegrationService.analyzeMismatches(anyList(), eq(COMPANY_ID)))
                .thenReturn(fakeAiResponse);
                
        when(auditLogRepository.save(any(PeriodAuditLog.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        PeriodAuditResponse response = auditorService.auditPeriod(request, COMPANY_ID);

        // Assert
        assertEquals("2026-11", response.getPeriod());
        assertEquals(fakeAiResponse, response.getAiRecommendation());
        assertFalse(response.isReadyToClose()); // Because our mock generates fake discrepancies in the service
        
        verify(depreciationService).calculateAndProvisionDepreciation(period, COMPANY_ID);
        verify(aiIntegrationService).analyzeMismatches(anyList(), eq(COMPANY_ID));
        verify(auditLogRepository).save(any(PeriodAuditLog.class));
    }
}
