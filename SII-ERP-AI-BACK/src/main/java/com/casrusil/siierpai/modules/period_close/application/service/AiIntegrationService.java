package com.casrusil.siierpai.modules.period_close.application.service;

import com.casrusil.siierpai.modules.period_close.domain.model.AiInsight;
import com.casrusil.siierpai.modules.period_close.domain.port.AiInsightRepositoryPort;
import lombok.RequiredArgsConstructor;
import dev.langchain4j.model.chat.ChatLanguageModel;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AiIntegrationService {

    private final AiInsightRepositoryPort aiInsightRepository;
    private final ChatLanguageModel chatLanguageModel;

    public String analyzeMismatches(List<String> discrepancies, String companyId) {
        if (discrepancies.isEmpty()) {
            return "No discrepancies found. The period is perfectly balanced.";
        }
        
        StringBuilder contextBuilder = new StringBuilder();
        contextBuilder.append("Found the following issues during period close:\n");
        discrepancies.forEach(d -> contextBuilder.append("- ").append(d).append("\n"));
        
        String prompt = "Eres un auditor contable experto (Tributación Chilena). Revisa las siguientes discrepancias encontradas en el cierre de mes e indica brevemente qué acción debe tomar el contador:\n" + contextBuilder.toString();
        
        String aiResponse;
        try {
            aiResponse = chatLanguageModel.generate(prompt);
        } catch (Exception e) {
            aiResponse = "Error conectando con la IA: " + e.getMessage();
        }
        
        AiInsight insight = AiInsight.builder()
                .id(UUID.randomUUID().toString())
                .companyId(companyId)
                .analysisContext(contextBuilder.toString())
                .generatedInsight(aiResponse)
                .generatedAt(LocalDateTime.now())
                .suggestedAction("Consultar reporte detallado de IA")
                .build();
                
        aiInsightRepository.save(insight);
        
        return aiResponse;
    }
}
