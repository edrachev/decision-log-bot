package com.decisionlog.bot.handler;

import com.decisionlog.domain.ConversationState;
import com.decisionlog.domain.Decision;
import com.decisionlog.service.ConversationService;
import com.decisionlog.service.DecisionService;
import com.decisionlog.service.MessageFormatter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReflectCommandHandlerTest {

    @Mock
    private DecisionService decisionService;

    @Mock
    private ConversationService conversationService;

    @Mock
    private MessageFormatter formatter;

    @InjectMocks
    private ReflectCommandHandler handler;

    private static final long CHAT_ID = 100L;

    @Test
    void handleCommand_withNoArgs_returnsUsageHint() {
        SendMessage result = handler.handleCommand(CHAT_ID, "");

        assertThat(result.getText()).contains("/reflect");
        assertThat(result.getText()).contains("id");
        verifyNoInteractions(decisionService);
    }

    @Test
    void handleCommand_withInvalidId_returnsError() {
        SendMessage result = handler.handleCommand(CHAT_ID, "abc");

        assertThat(result.getText()).contains("Некорректный id");
        verifyNoInteractions(decisionService);
    }

    @Test
    void handleCommand_withNonExistentId_returnsNotFound() {
        when(decisionService.findById(99L)).thenReturn(Optional.empty());

        SendMessage result = handler.handleCommand(CHAT_ID, "99");

        assertThat(result.getText()).contains("#99");
        assertThat(result.getText()).contains("не найдено");
    }

    @Test
    void handleCommand_withValidId_startsReflectionFlow() {
        Decision d = makeDecision("Мигрировать на K8s");
        when(decisionService.findById(7L)).thenReturn(Optional.of(d));

        SendMessage result = handler.handleCommand(CHAT_ID, "7");

        verify(conversationService).reset();
        verify(conversationService).setState(ConversationState.WAITING_ACTUAL_RESULT);
        verify(conversationService).setPendingReflectId(7L);
        assertThat(result.getText()).contains("Шаг 1/2");
        assertThat(result.getText()).contains("Мигрировать на K8s");
    }

    @Test
    void handleInput_whenWaitingActualResult_storesAndAsksForLesson() {
        when(conversationService.getState()).thenReturn(ConversationState.WAITING_ACTUAL_RESULT);

        SendMessage result = handler.handleInput(CHAT_ID, "Выкатили без проблем");

        verify(conversationService).putBuffer("actualResult", "Выкатили без проблем");
        verify(conversationService).setState(ConversationState.WAITING_LESSON);
        assertThat(result.getText()).contains("Шаг 2/2");
    }

    @Test
    void handleInput_whenWaitingLesson_savesReflectionAndReturnsDecision() {
        when(conversationService.getState()).thenReturn(ConversationState.WAITING_LESSON);
        when(conversationService.getPendingReflectId()).thenReturn(7L);
        when(conversationService.getBuffer("actualResult")).thenReturn("Выкатили без проблем");

        Decision updated = makeDecision("Мигрировать на K8s");
        updated.setActualResult("Выкатили без проблем");
        updated.setLesson("Нужен staging стенд");
        when(decisionService.addReflection(7L, "Выкатили без проблем", "Нужен staging стенд"))
                .thenReturn(Optional.of(updated));
        when(formatter.formatFull(updated)).thenReturn("formatted decision");

        SendMessage result = handler.handleInput(CHAT_ID, "Нужен staging стенд");

        verify(conversationService).reset();
        assertThat(result.getText()).contains("Рефлексия сохранена");
        assertThat(result.getText()).contains("formatted decision");
    }

    @Test
    void handleInput_whenLessonIsDash_savesEmptyLesson() {
        when(conversationService.getState()).thenReturn(ConversationState.WAITING_LESSON);
        when(conversationService.getPendingReflectId()).thenReturn(7L);
        when(conversationService.getBuffer("actualResult")).thenReturn("Нормально");

        Decision updated = makeDecision("Решение");
        when(decisionService.addReflection(7L, "Нормально", "")).thenReturn(Optional.of(updated));
        when(formatter.formatFull(updated)).thenReturn("formatted");

        handler.handleInput(CHAT_ID, "-");

        verify(decisionService).addReflection(7L, "Нормально", "");
    }

    private Decision makeDecision(String decisionText) {
        Decision d = new Decision();
        d.setContext("Контекст");
        d.setDecision(decisionText);
        d.setExpectedResult("Ожидаемый результат");
        return d;
    }
}
