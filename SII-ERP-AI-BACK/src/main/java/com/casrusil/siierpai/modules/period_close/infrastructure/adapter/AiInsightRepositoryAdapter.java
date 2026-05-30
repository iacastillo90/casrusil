package com.casrusil.siierpai.modules.period_close.infrastructure.adapter;

import com.casrusil.siierpai.modules.period_close.domain.model.AiInsight;
import com.casrusil.siierpai.modules.period_close.domain.port.AiInsightRepositoryPort;
import com.casrusil.siierpai.modules.period_close.infrastructure.persistence.entity.AiInsightEntity;
import com.casrusil.siierpai.modules.period_close.infrastructure.persistence.repository.AiInsightJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AiInsightRepositoryAdapter implements AiInsightRepositoryPort {

    private final AiInsightJpaRepository jpaRepository;

    @Override
    public AiInsight save(AiInsight insight) {
        AiInsightEntity entity = mapToEntity(insight);
        return mapToDomain(jpaRepository.save(entity));
    }
    
    private AiInsightEntity mapToEntity(AiInsight domain) {
        AiInsightEntity entity = new AiInsightEntity();
        entity.setId(domain.getId());
        entity.setCompanyId(domain.getCompanyId());
        entity.setAnalysisContext(domain.getAnalysisContext());
        entity.setGeneratedInsight(domain.getGeneratedInsight());
        entity.setGeneratedAt(domain.getGeneratedAt());
        entity.setSuggestedAction(domain.getSuggestedAction());
        return entity;
    }
    
    private AiInsight mapToDomain(AiInsightEntity entity) {
        return AiInsight.builder()
                .id(entity.getId())
                .companyId(entity.getCompanyId())
                .analysisContext(entity.getAnalysisContext())
                .generatedInsight(entity.getGeneratedInsight())
                .generatedAt(entity.getGeneratedAt())
                .suggestedAction(entity.getSuggestedAction())
                .build();
    }
}
