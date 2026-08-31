package com.casrusil.siierpai.modules.period_close.application.service;

import com.casrusil.siierpai.modules.period_close.domain.dto.PeriodAuditResponse;
import com.casrusil.siierpai.modules.period_close.domain.dto.PeriodCloseRequest;
import com.casrusil.siierpai.modules.period_close.domain.model.PeriodAuditLog;
import com.casrusil.siierpai.modules.period_close.domain.port.PeriodAuditLogRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PeriodCloseAuditorService implements PeriodCloseUseCase {

    private final PeriodAuditLogRepositoryPort auditLogRepository;
    private final AiIntegrationService aiIntegrationService;
    private final DepreciationServiceImpl depreciationService;

    @Override
    @Transactional
    public PeriodAuditResponse auditPeriod(PeriodCloseRequest request, String companyId) {
        YearMonth period = YearMonth.of(request.getYear(), request.getMonth());
        List<String> discrepancies = new ArrayList<>();
        
        // Mocking the complex audit logic:
        // 1. Cross-check BankTransactions vs JournalEntries (Bank Reconciliation)
        discrepancies.add("Missing bank reconciliation for transaction: $50,000 on 15th");
        
        // 2. Cross-check DTEs vs JournalEntries
        discrepancies.add("Uncentralized purchase invoice #99432");
        
        // 3. Process Depreciation
        List<String> depLogs = depreciationService.calculateAndProvisionDepreciation(period, companyId);
        
        // 4. Send findings to AI
        String aiRecommendation = aiIntegrationService.analyzeMismatches(discrepancies, companyId);
        
        // 5. Save audit log
        PeriodAuditLog auditLog = PeriodAuditLog.builder()
                .companyId(companyId)
                .period(period)
                .auditDate(LocalDateTime.now())
                .issuesFound(discrepancies)
                .isResolved(discrepancies.isEmpty())
                .build();
                
        auditLogRepository.save(auditLog);
        
        return PeriodAuditResponse.builder()
                .period(period.toString())
                .issuesFound(discrepancies)
                .readyToClose(discrepancies.isEmpty())
                .aiRecommendation(aiRecommendation)
                .build();
    }

    @Override
    public void executeClose(PeriodCloseRequest request, String companyId) {
        YearMonth period = YearMonth.of(request.getYear(), request.getMonth());
        
        PeriodAuditLog log = auditLogRepository.findByPeriodAndCompanyId(period, companyId)
                .orElseThrow(() -> new RuntimeException("Must audit period before closing"));
                
        if (!log.isResolved()) {
            throw new RuntimeException("Cannot close period. There are unresolved discrepancies.");
        }
        
        // Logic to seal the AccountingPeriod
    }
}
