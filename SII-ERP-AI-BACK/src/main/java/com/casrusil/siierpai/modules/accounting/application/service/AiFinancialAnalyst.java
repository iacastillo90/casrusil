package com.casrusil.siierpai.modules.accounting.application.service;

import com.casrusil.siierpai.modules.dms.infrastructure.adapter.out.ai.GeminiApiClient;
import org.springframework.stereotype.Service;

/**
 * Analista Financiero potenciado por IA (Google Gemini).
 * Genera resúmenes ejecutivos narrativos con inteligencia artificial real.
 */
public interface AiFinancialAnalyst {
    String generateExecutiveSummary(String context);
}

@Service
class GeminiFinancialAnalyst implements AiFinancialAnalyst {

    private final GeminiApiClient geminiClient;

    GeminiFinancialAnalyst(GeminiApiClient geminiClient) {
        this.geminiClient = geminiClient;
    }

    @Override
    public String generateExecutiveSummary(String context) {
        String prompt = "Eres un analista financiero senior especializado en PYMEs chilenas. " +
                "Basándote en los siguientes datos financieros del período, genera un " +
                "resumen ejecutivo conciso (máx. 3 párrafos) con los hallazgos más importantes y " +
                "recomendaciones estratégicas:\n\n" + context;
        return geminiClient.analyzeInvoiceDocument(prompt.getBytes(), prompt);
    }
}
