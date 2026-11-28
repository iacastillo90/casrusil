package com.casrusil.siierpai.modules.ai_assistant.application.service;

import com.casrusil.siierpai.modules.ai_assistant.domain.model.Conversation;
import com.casrusil.siierpai.modules.ai_assistant.domain.model.Message;
import com.casrusil.siierpai.modules.ai_assistant.domain.model.Tool;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import com.casrusil.siierpai.shared.domain.valueobject.UserId;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.output.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

class ConversationServiceTest {

    private ConversationService conversationService;

    @Mock
    private ChatLanguageModel chatLanguageModel;

    @Mock
    private Tool mockTool;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mockTool.name()).thenReturn("mockTool");
        when(mockTool.description()).thenReturn("A mock tool");

        conversationService = new ConversationService(chatLanguageModel, List.of(mockTool));
    }

    @Test
    void startConversation_ShouldCreateNewConversation() {
        CompanyId companyId = CompanyId.random();
        UserId userId = UserId.random();

        Conversation conversation = conversationService.startConversation(companyId, userId);

        assertNotNull(conversation);
        assertEquals(companyId, conversation.getCompanyId());
        assertEquals(userId, conversation.getUserId());
        assertNotNull(conversation.getId());
    }

    @Test
    void sendMessage_ShouldAddUserAndAiMessages() {
        // Given
        CompanyId companyId = CompanyId.random();
        UserId userId = UserId.random();
        Conversation conversation = conversationService.startConversation(companyId, userId);
        String userContent = "Hello AI";
        String aiContent = "Hello User";

        when(chatLanguageModel.generate(anyList(), anyList())).thenReturn(Response.from(new AiMessage(aiContent)));

        // When
        Message responseMsg = inCompany(companyId,
                () -> conversationService.sendMessage(conversation.getId(), userId, userContent));

        // Then
        assertNotNull(responseMsg);
        assertEquals("assistant", responseMsg.role());
        assertEquals(aiContent, responseMsg.content());

        List<Message> messages = conversation.getMessages();
        assertEquals(2, messages.size());
        assertEquals("user", messages.get(0).role());
        assertEquals(userContent, messages.get(0).content());
        assertEquals("assistant", messages.get(1).role());
        assertEquals(aiContent, messages.get(1).content());
    }

    @Test
    void sendMessage_conversacionDeOtraEmpresa_respondeNoEncontrada() {
        CompanyId companyA = CompanyId.random();
        UserId userA = UserId.random();
        Conversation conversationA = conversationService.startConversation(companyA, userA);

        CompanyId companyB = CompanyId.random();
        assertThrows(com.casrusil.siierpai.shared.domain.exception.ResourceNotFoundException.class,
                () -> inCompany(companyB, () -> conversationService.sendMessage(conversationA.getId(), userA, "hola")));
        assertTrue(conversationA.getMessages().isEmpty(), "No debe escribir en la conversación ajena");
    }

    @Test
    void sendMessage_conversacionDeOtroUsuarioMismaEmpresa_respondeNoEncontrada() {
        CompanyId company = CompanyId.random();
        Conversation conversation = conversationService.startConversation(company, UserId.random());

        assertThrows(com.casrusil.siierpai.shared.domain.exception.ResourceNotFoundException.class,
                () -> inCompany(company, () -> conversationService.sendMessage(conversation.getId(), UserId.random(), "hola")));
    }

    @Test
    void sendMessage_errorDelProveedor_noExponeMensajeCrudo() {
        CompanyId company = CompanyId.random();
        UserId user = UserId.random();
        Conversation conversation = conversationService.startConversation(company, user);
        when(chatLanguageModel.generate(anyList(), anyList()))
                .thenThrow(new RuntimeException("API key invalid: sk-secreto quota exceeded"));

        Message reply = inCompany(company, () -> conversationService.sendMessage(conversation.getId(), user, "hola"));

        assertFalse(reply.content().contains("sk-secreto"));
        assertFalse(reply.content().contains("API key"));
        assertTrue(reply.content().contains("referencia"));
    }

    private static <T> T inCompany(CompanyId companyId, java.util.concurrent.Callable<T> action) {
        try {
            return java.lang.ScopedValue
                    .where(com.casrusil.siierpai.shared.infrastructure.context.CompanyContext.COMPANY_ID, companyId)
                    .call(action::call);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
