package com.casrusil.siierpai.modules.dms.application.service;

import com.casrusil.siierpai.modules.dms.infrastructure.adapter.out.ai.GeminiApiClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class InvoiceAiReaderService {

    private final GeminiApiClient geminiClient;
    private final ObjectMapper objectMapper;

    public InvoiceAiReaderService(GeminiApiClient geminiClient, ObjectMapper objectMapper) {
        this.geminiClient = geminiClient;
        this.objectMapper = objectMapper;
    }

    public record ExtractedInvoiceData(
            String rutProveedor,
            String razonSocial,
            LocalDate fechaEmision,
            long montoNeto,
            long montoIva,
            long montoTotal,
            int folio
    ) {}

    public ExtractedInvoiceData readInvoice(byte[] fileBytes) {
        String prompt = "Eres un asistente contable chileno. Extrae los siguientes datos de esta factura: " +
                "RUT del proveedor, Razón Social, Fecha de Emisión (YYYY-MM-DD), Monto Neto, IVA y Monto Total, y Folio. " +
                "Devuelve SOLO un objeto JSON válido, sin texto adicional.";

        String aiResponse = geminiClient.analyzeInvoiceDocument(fileBytes, prompt);

        try {
            JsonNode root = objectMapper.readTree(aiResponse);
            return new ExtractedInvoiceData(
                    root.get("rutProveedor").asText(),
                    root.get("razonSocial").asText(),
                    LocalDate.parse(root.get("fechaEmision").asText()),
                    root.get("montoNeto").asLong(),
                    root.get("montoIva").asLong(),
                    root.get("montoTotal").asLong(),
                    root.get("folio").asInt()
            );
        } catch (Exception e) {
            throw new RuntimeException("Error parseando respuesta de Gemini AI", e);
        }
    }
}
