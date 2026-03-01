package com.decisionlog.bot;

import com.decisionlog.bot.handler.AddCommandHandler;
import com.decisionlog.bot.handler.ListCommandHandler;
import com.decisionlog.bot.handler.ReflectCommandHandler;
import com.decisionlog.bot.handler.ViewCommandHandler;
import com.decisionlog.config.BotConfig;
import com.decisionlog.domain.ConversationState;
import com.decisionlog.service.ConversationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
@Component
public class DecisionLogBot extends TelegramWebhookBot {

    private final BotConfig config;
    private final ConversationService conversationService;
    private final AddCommandHandler addHandler;
    private final ListCommandHandler listHandler;
    private final ReflectCommandHandler reflectHandler;
    private final ViewCommandHandler viewHandler;

    public DecisionLogBot(BotConfig config,
                          ConversationService conversationService,
                          AddCommandHandler addHandler,
                          ListCommandHandler listHandler,
                          ReflectCommandHandler reflectHandler,
                          ViewCommandHandler viewHandler) {
        super(config.getToken());
        this.config = config;
        this.conversationService = conversationService;
        this.addHandler = addHandler;
        this.listHandler = listHandler;
        this.reflectHandler = reflectHandler;
        this.viewHandler = viewHandler;
    }

    @Override
    public String getBotUsername() {
        return config.getUsername();
    }

    @Override
    public String getBotPath() {
        return config.getUsername();
    }

    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return null;
        }

        Message message = update.getMessage();
        long userId = message.getFrom().getId();
        long chatId = message.getChatId();

        if (userId != config.getAllowedUserId()) {
            log.warn("Unauthorized access attempt from userId={}", userId);
            return deny(chatId);
        }

        String text = message.getText().trim();
        log.info("Received message from userId={}: {}", userId, text);

        return route(chatId, text);
    }

    private BotApiMethod<?> route(long chatId, String text) {
        // Команды всегда имеют приоритет и сбрасывают текущий диалог
        if (text.startsWith("/start") || text.startsWith("/help")) {
            conversationService.reset();
            return help(chatId);
        }
        if (text.startsWith("/add")) {
            return addHandler.handleCommand(chatId);
        }
        if (text.startsWith("/list")) {
            return listHandler.handle(chatId);
        }
        if (text.startsWith("/view")) {
            return viewHandler.handle(chatId, extractArgs(text, "/view"));
        }
        if (text.startsWith("/reflect")) {
            String args = extractArgs(text, "/reflect");
            if (!args.isBlank()) {
                // Команда с аргументом — начинаем диалог
                return reflectHandler.handleCommand(chatId, args);
            }
        }
        if (text.startsWith("/cancel")) {
            conversationService.reset();
            return plain(chatId, "Отменено. Что делаем дальше?");
        }

        // Продолжение активного диалога
        ConversationState state = conversationService.getState();
        if (state == ConversationState.WAITING_CONTEXT
                || state == ConversationState.WAITING_DECISION
                || state == ConversationState.WAITING_EXPECTED_RESULT) {
            return addHandler.handleInput(chatId, text);
        }
        if (state == ConversationState.WAITING_ACTUAL_RESULT
                || state == ConversationState.WAITING_LESSON) {
            return reflectHandler.handleInput(chatId, text);
        }

        return help(chatId);
    }

    private SendMessage help(long chatId) {
        String helpText = """
                *Decision Log Bot*

                Логирую управленческие решения и рефлексию по ним.

                *Команды:*
                /add — добавить новое решение
                /list — последние 10 решений
                /view \\[id\\] — посмотреть решение полностью
                /reflect \\[id\\] — добавить рефлексию к решению
                /cancel — отменить текущее действие

                ✅ — решение с рефлексией
                """;
        return md(chatId, helpText);
    }

    private SendMessage deny(long chatId) {
        return plain(chatId, "Доступ запрещён.");
    }

    private SendMessage plain(long chatId, String text) {
        SendMessage msg = new SendMessage();
        msg.setChatId(chatId);
        msg.setText(text);
        return msg;
    }

    private SendMessage md(long chatId, String text) {
        SendMessage msg = new SendMessage();
        msg.setChatId(chatId);
        msg.setText(text);
        msg.setParseMode("Markdown");
        return msg;
    }

    private String extractArgs(String text, String command) {
        if (text.length() <= command.length()) return "";
        String rest = text.substring(command.length()).trim();
        // Убираем @botusername если есть
        if (rest.startsWith("@")) {
            int spaceIdx = rest.indexOf(' ');
            if (spaceIdx < 0) return "";
            rest = rest.substring(spaceIdx).trim();
        }
        return rest;
    }
}
