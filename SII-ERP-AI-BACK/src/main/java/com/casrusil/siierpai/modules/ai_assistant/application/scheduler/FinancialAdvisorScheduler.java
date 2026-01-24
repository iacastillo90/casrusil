package com.casrusil.siierpai.modules.ai_assistant.application.scheduler;

import com.casrusil.siierpai.modules.ai_assistant.domain.service.FinancialContextBuilder;
import com.casrusil.siierpai.modules.sso.domain.model.Company;
import com.casrusil.siierpai.modules.sso.domain.port.out.CompanyRepository;
import com.casrusil.siierpai.shared.infrastructure.context.CompanyContext;
import com.casrusil.siierpai.shared.infrastructure.mail.EmailService;
import dev.langchain4j.model.chat.ChatLanguageModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * Scheduler que envía análisis financiero diario a todas las empresas activas.
 * Utiliza datos REALES extraídos de F29, Facturas, Cash Flow y Auditoría.
 */
@Component
public class FinancialAdvisorScheduler {

    private static final Logger logger = LoggerFactory.getLogger(FinancialAdvisorScheduler.class);

    private final EmailService emailService;
    private final ChatLanguageModel chatLanguageModel;
    private final FinancialContextBuilder contextBuilder;
    private final CompanyRepository companyRepository;

    @Value("${spring.mail.username}")
    private String adminEmail; // Fallback email

    public FinancialAdvisorScheduler(EmailService emailService,
            ChatLanguageModel chatLanguageModel,
            FinancialContextBuilder contextBuilder,
            CompanyRepository companyRepository) {
        this.emailService = emailService;
        this.chatLanguageModel = chatLanguageModel;
        this.contextBuilder = contextBuilder;
        this.companyRepository = companyRepository;
    }

    /**
     * Ejecuta el análisis financiero diario a las 8:00 AM.
     * Procesa TODAS las empresas activas en paralelo usando Virtual Threads.
     */
    @Scheduled(cron = "0 0 8 * * *")
    public void sendDailyBriefing() {
        logger.info("🚀 Iniciando análisis financiero diario con datos REALES...");

        try {
            // Obtener todas las empresas activas
            List<Company> companies = companyRepository.findAll();
            logger.info("Procesando {} empresas", companies.size());

            // Procesar cada empresa en su propio contexto
            for (Company company : companies) {
                try {
                    // CRÍTICO: Establecer CompanyContext para que todos los servicios
                    // downstream (F29Calculator, InvoiceRepository, etc.) funcionen correctamente
                    CompanyContext.runInCompanyContext(company.getId(), () -> {
                        processCompanyBriefing(company);
                    });
                } catch (Exception e) {
                    logger.error("❌ Error procesando empresa: {}", company.getRazonSocial(), e);
                }
            }

            logger.info("✅ Análisis financiero diario completado");

        } catch (Exception e) {
            logger.error("❌ Error crítico en Financial Advisor Scheduler", e);
        }
    }

    /**
     * Procesa el briefing financiero para una empresa específica.
     * Este método se ejecuta dentro del CompanyContext.
     */
    private void processCompanyBriefing(Company company) {
        logger.info("📊 Generando briefing para: {}", company.getRazonSocial());

        // 1. Construir contexto con datos REALES
        String financialContext = contextBuilder.buildDailyContext(company.getId());

        // 2. Prompt estratégico para Gemini
        String prompt = String.format("""
                Eres el CFO (Director Financiero) de la empresa '%s'.
                Analiza los siguientes datos financieros REALES del día:

                %s

                Tu misión:
                1. Detectar riesgos inmediatos (ej: mucho IVA a pagar, facturas vencidas, déficit proyectado).
                2. Identificar oportunidades (IVA recuperable, optimizaciones).
                3. Sugerir 2-3 acciones concretas para HOY.

                Formato:
                - Usa bullets (•) para listar puntos
                - Sé directo y ejecutivo
                - No saludes genéricamente
                - Enfócate en números y acciones
                """, company.getRazonSocial(), financialContext);

        // 3. Generar análisis con IA
        String advice = chatLanguageModel.generate(prompt);

        // 4. Enviar correo
        String recipientEmail = company.getEmail() != null ? company.getEmail() : adminEmail;
        String subject = String.format("📊 Briefing Financiero: %s - %s",
                company.getRazonSocial(),
                LocalDate.now());

        String body = String.format("""
                <html>
                <head>
                    <style>
                        body { font-family: 'Segoe UI', Arial, sans-serif; }
                        .header { background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%);
                                 color: white; padding: 20px; border-radius: 8px 8px 0 0; }
                        .content { background-color: #f8f9fa; padding: 20px;
                                  border-left: 5px solid #667eea; margin: 20px 0; }
                        .footer { color: #6c757d; font-size: 12px; margin-top: 20px; }
                    </style>
                </head>
                <body>
                    <div class="header">
                        <h2 style="margin: 0;">🧠 Análisis Financiero Inteligente</h2>
                        <p style="margin: 5px 0 0 0; opacity: 0.9;">%s</p>
                    </div>
                    <div class="content">
                        %s
                    </div>
                    <div class="footer">
                        <p>📅 Datos actualizados al momento de generación</p>
                        <p>🔮 Generado por SII-ERP-AI con IA Predictiva (Gemini)</p>
                    </div>
                </body>
                </html>
                """,
                company.getRazonSocial(),
                advice.replace("\n", "<br/>"));

        emailService.sendEmail(recipientEmail, subject, body);
        logger.info("✉️ Briefing enviado a: {}", recipientEmail);
    }
}
