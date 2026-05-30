package com.casrusil.siierpai.modules.period_close.domain.model;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class AiInsight {
    private String id;
    private String companyId;
    private String analysisContext; // E.g., "Bank Reconciliation Mismatches"
    private String generatedInsight; // The text provided by LLM
    private LocalDateTime generatedAt;
    private String suggestedAction; // E.g., "Create adjustment journal entry"
}
