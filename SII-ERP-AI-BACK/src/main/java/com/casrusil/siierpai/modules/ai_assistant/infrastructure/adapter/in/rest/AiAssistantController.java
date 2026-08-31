package com.casrusil.siierpai.modules.ai_assistant.infrastructure.adapter.in.rest;

import jakarta.validation.Valid;

import com.casrusil.siierpai.modules.ai_assistant.application.service.ConversationService;
import com.casrusil.siierpai.modules.ai_assistant.domain.model.Conversation;
import com.casrusil.siierpai.modules.ai_assistant.domain.model.Message;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import com.casrusil.siierpai.shared.domain.valueobject.UserId;
import com.casrusil.siierpai.shared.infrastructure.context.CompanyContext;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Controlador REST para el Asistente de IA Financiero.
 * 
 * <p>
 * Gestiona las conversaciones entre el usuario y el modelo de IA (Gemini).
 * Permite iniciar nuevas sesiones y enviar mensajes, manteniendo el contexto
 * de la empresa actual.
 * 
 * <h2>Endpoints:</h2>
 * <ul>
 * <li>{@code POST /api/v1/ai/conversations}: Iniciar nueva conversación.</li>
 * <li>{@code POST /api/v1/ai/conversations/{id}/messages}: Enviar mensaje al
 * asistente.</li>
 * </ul>
 * 
 * @see ConversationService
 * @since 1.0
 */
@RestController
@RequestMapping("/api/v1/ai")
public class AiAssistantController {

    private final ConversationService conversationService;

    public AiAssistantController(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    @PostMapping("/conversations")
    public ResponseEntity<Conversation> startConversation() {
        CompanyId companyId = CompanyContext.requireCompanyId();
        // Extraer UserId real del SecurityContextHolder (seteado por SecurityFilter desde JWT).
        // El principal es el UserId que fue puesto en el token durante el login.
        UserId userId = getCurrentUserId();
        return ResponseEntity.ok(conversationService.startConversation(companyId, userId));
    }

    @PostMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<Message> sendMessage(
            @PathVariable UUID conversationId,
            @Valid @RequestBody SendMessageRequest request) {
        return ResponseEntity.ok(conversationService.sendMessage(conversationId, request.content()));
    }

    /**
     * Extrae el UserId del usuario autenticado desde el SecurityContextHolder.
     * El principal fue establecido por {@code SecurityFilter} al validar el JWT.
     */
    private UserId getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserId userId) {
            return userId;
        }
        // Fallback: no debería ocurrir si el endpoint está protegido correctamente
        throw new IllegalStateException("No se pudo obtener el UserId del contexto de seguridad. " +
                "Verifica que el endpoint esté correctamente autenticado.");
    }

    public record SendMessageRequest(String content) {
    }
}
