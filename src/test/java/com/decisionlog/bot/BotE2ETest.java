package com.decisionlog.bot;

import com.decisionlog.service.ConversationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class BotE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ConversationService conversationService;

    // Мокируем регистратор — чтобы не обращался к Telegram API при старте
    @MockBean
    private WebhookRegistrar webhookRegistrar;

    private static final long ALLOWED_USER = 12345L;
    private static final long UNKNOWN_USER  = 99999L;

    @BeforeEach
    void resetConversation() {
        conversationService.reset();
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Безопасность
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    void unauthorizedUser_isRejected() throws Exception {
        send("/start", UNKNOWN_USER)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("Доступ запрещён."));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // /add — полный трёхшаговый флоу
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    void addCommand_startsConversationAndAsksForContext() throws Exception {
        send("/add", ALLOWED_USER)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value(org.hamcrest.Matchers.containsString("Шаг 1/3")));
    }

    @Test
    void fullAddFlow_savesDecisionAndConfirms() throws Exception {
        send("/add", ALLOWED_USER)
                .andExpect(jsonPath("$.text").value(org.hamcrest.Matchers.containsString("Шаг 1/3")));

        send("Команда растёт, нужно выстроить процессы", ALLOWED_USER)
                .andExpect(jsonPath("$.text").value(org.hamcrest.Matchers.containsString("Шаг 2/3")));

        send("Внедрить еженедельные 1-on-1", ALLOWED_USER)
                .andExpect(jsonPath("$.text").value(org.hamcrest.Matchers.containsString("Шаг 3/3")));

        send("Повысить вовлечённость и выявить проблемы раньше", ALLOWED_USER)
                .andExpect(jsonPath("$.text").value(org.hamcrest.Matchers.containsString("сохранено")))
                .andExpect(jsonPath("$.text").value(org.hamcrest.Matchers.containsString("/reflect")));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // /list
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    void listCommand_whenNoDecisions_showsEmptyHint() throws Exception {
        send("/list", ALLOWED_USER)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value(org.hamcrest.Matchers.containsString("/add")));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // /view
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    void viewCommand_nonExistentId_returnsNotFound() throws Exception {
        send("/view 999", ALLOWED_USER)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value(org.hamcrest.Matchers.containsString("не найдено")));
    }

    @Test
    void viewCommand_invalidId_returnsError() throws Exception {
        send("/view abc", ALLOWED_USER)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value(org.hamcrest.Matchers.containsString("Некорректный id")));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // /cancel
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    void cancelCommand_duringSddFlow_resetsConversation() throws Exception {
        send("/add", ALLOWED_USER);
        send("некий контекст", ALLOWED_USER);

        send("/cancel", ALLOWED_USER)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value(org.hamcrest.Matchers.containsString("Отменено")));

        // После отмены — снова /add начинает с нуля
        send("/add", ALLOWED_USER)
                .andExpect(jsonPath("$.text").value(org.hamcrest.Matchers.containsString("Шаг 1/3")));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Helper
    // ──────────────────────────────────────────────────────────────────────────

    private ResultActions send(String text, long userId) throws Exception {
        String body = update(text, userId);
        return mockMvc.perform(
                post("/testbot")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
        );
    }

    private String update(String text, long userId) {
        return """
                {
                  "update_id": 1,
                  "message": {
                    "message_id": 1,
                    "from": { "id": %d, "first_name": "Test", "is_bot": false },
                    "chat": { "id": %d, "type": "private" },
                    "text": "%s",
                    "date": 1700000000
                  }
                }
                """.formatted(userId, userId, text);
    }
}
