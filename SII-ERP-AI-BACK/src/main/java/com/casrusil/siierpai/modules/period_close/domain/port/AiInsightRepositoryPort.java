package com.casrusil.siierpai.modules.period_close.domain.port;

import com.casrusil.siierpai.modules.period_close.domain.model.AiInsight;

public interface AiInsightRepositoryPort {
    AiInsight save(AiInsight insight);
}
