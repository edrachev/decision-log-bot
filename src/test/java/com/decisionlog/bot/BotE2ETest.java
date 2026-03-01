package com.decisionlog.bot;

import com.decisionlog.service.ConversationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.generics.BotSession;
import org.telegram.telegrambots.meta.generics.LongPollingBot;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

@SpringBootTest
@ActiveProfiles("test")
class BotE2ETest {

    @Autowired
    private DecisionLogBot bot;

    @Autowired
    private ConversationService conversationService;

    // Мокируем BotSession — чтобы polling не стартовал при поднятии контекста
    @MockBean(name = "botSession")
    private BotSession botSession;

    private static final long ALLOWED_USER = 12345L;
    private static final long UNKNOWN_USER  = 99999L;

    @BeforeEach
    void reset() {
        conversationService.reset();
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Безопасность
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    void unauthorizedUser_isRejected() {
        SendMessage response = bot.route(UNKNOWN_USER, "/start");
        assertThat(response.getText()).isEqualTo("Доступ запрещён.");
    }

    // ──────────────────────────────────────────────────────────────────────────
    // /add — полный трёхшаговый флоу
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    void addCommand_startsConversationAndAsksForContext() {
        SendMessage response = bot.route(ALLOWED_USER, "/add");
        assertThat(response.getText()).contains("Шаг 1/3");
    }

    @Test
    void fullAddFlow_savesDecisionAndConfirms() {
        assertThat(bot.route(ALLOWED_USER, "/add").getText()).contains("Шаг 1/3");
        assertThat(bot.route(ALLOWED_USER, "Команда растёт").getText()).contains("Шаг 2/3");
        assertThat(bot.route(ALLOWED_USER, "Внедрить 1-on-1").getText()).contains("Шаг 3/3");

        SendMessage result = bot.route(ALLOWED_USER, "Повысить вовлечённость");
        assertThat(result.getText()).contains("сохранено");
        assertThat(result.getText()).contains("/reflect");
    }

    // ──────────────────────────────────────────────────────────────────────────
    // /list
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    void listCommand_whenNoDecisions_showsEmptyHint() {
        SendMessage response = bot.route(ALLOWED_USER, "/list");
        assertThat(response.getText()).contains("/add");
    }

    // ──────────────────────────────────────────────────────────────────────────
    // /view
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    void viewCommand_nonExistentId_returnsNotFound() {
        SendMessage response = bot.route(ALLOWED_USER, "/view 999");
        assertThat(response.getText()).contains("не найдено");
    }

    @Test
    void viewCommand_invalidId_returnsError() {
        SendMessage response = bot.route(ALLOWED_USER, "/view abc");
        assertThat(response.getText()).contains("Некорректный id");
    }

    // ──────────────────────────────────────────────────────────────────────────
    // /cancel
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    void cancelCommand_duringAddFlow_resetsConversation() {
        bot.route(ALLOWED_USER, "/add");
        bot.route(ALLOWED_USER, "какой-то контекст");

        assertThat(bot.route(ALLOWED_USER, "/cancel").getText()).contains("Отменено");
        assertThat(bot.route(ALLOWED_USER, "/add").getText()).contains("Шаг 1/3");
    }
}
