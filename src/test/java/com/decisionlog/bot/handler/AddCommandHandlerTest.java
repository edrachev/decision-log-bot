package com.decisionlog.bot.handler;

import com.decisionlog.domain.ConversationState;
import com.decisionlog.domain.Decision;
import com.decisionlog.service.ConversationService;
import com.decisionlog.service.DecisionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddCommandHandlerTest {

    @Mock
    private DecisionService decisionService;

    @Mock
    private ConversationService conversationService;

    @InjectMocks
    private AddCommandHandler handler;

    private static final long CHAT_ID = 100L;

    @Test
    void handleCommand_resetsStateAndAsksForContext() {
        SendMessage result = handler.handleCommand(CHAT_ID);

        verify(conversationService).reset();
        verify(conversationService).setState(ConversationState.WAITING_CONTEXT);
        assertThat(result.getChatId()).isEqualTo(String.valueOf(CHAT_ID));
        assertThat(result.getText()).contains("Шаг 1/3");
        assertThat(result.getText()).contains("контекст");
    }

    @Test
    void handleInput_whenWaitingContext_storesAndAsksForDecision() {
        when(conversationService.getState()).thenReturn(ConversationState.WAITING_CONTEXT);

        SendMessage result = handler.handleInput(CHAT_ID, "Выбор CI/CD инструмента");

        verify(conversationService).putBuffer("context", "Выбор CI/CD инструмента");
        verify(conversationService).setState(ConversationState.WAITING_DECISION);
        assertThat(result.getText()).contains("Шаг 2/3");
    }

    @Test
    void handleInput_whenWaitingDecision_storesAndAsksForExpectedResult() {
        when(conversationService.getState()).thenReturn(ConversationState.WAITING_DECISION);

        SendMessage result = handler.handleInput(CHAT_ID, "Использовать GitHub Actions");

        verify(conversationService).putBuffer("decision", "Использовать GitHub Actions");
        verify(conversationService).setState(ConversationState.WAITING_EXPECTED_RESULT);
        assertThat(result.getText()).contains("Шаг 3/3");
    }

    @Test
    void handleInput_whenWaitingExpectedResult_savesDecisionAndResetsState() {
        when(conversationService.getState()).thenReturn(ConversationState.WAITING_EXPECTED_RESULT);
        when(conversationService.getBuffer("context")).thenReturn("Выбор CI/CD");
        when(conversationService.getBuffer("decision")).thenReturn("GitHub Actions");

        Decision saved = new Decision();
        setId(saved, 42L);
        when(decisionService.save("Выбор CI/CD", "GitHub Actions", "Автоматизация деплоя"))
                .thenReturn(saved);

        SendMessage result = handler.handleInput(CHAT_ID, "Автоматизация деплоя");

        verify(decisionService).save("Выбор CI/CD", "GitHub Actions", "Автоматизация деплоя");
        verify(conversationService).reset();
        assertThat(result.getText()).contains("#42");
        assertThat(result.getText()).contains("/reflect 42");
    }

    @Test
    void handleInput_inUnexpectedState_returnsErrorHint() {
        when(conversationService.getState()).thenReturn(ConversationState.IDLE);

        SendMessage result = handler.handleInput(CHAT_ID, "что-то");

        assertThat(result.getText()).contains("/add");
    }

    private void setId(Decision d, Long id) {
        try {
            Field f = Decision.class.getDeclaredField("id");
            f.setAccessible(true);
            f.set(d, id);
        } catch (Exception e) { throw new RuntimeException(e); }
    }
}
