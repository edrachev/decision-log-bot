package com.decisionlog.bot.handler;

import com.decisionlog.domain.ConversationState;
import com.decisionlog.service.ConversationService;
import com.decisionlog.service.DecisionService;
import com.decisionlog.service.MessageFormatter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@Component
@RequiredArgsConstructor
public class ReflectCommandHandler {

    private final DecisionService decisionService;
    private final ConversationService conversationService;
    private final MessageFormatter formatter;

    public SendMessage handleCommand(long chatId, String args) {
        if (args == null || args.isBlank()) {
            return text(chatId, "Укажите id решения: /reflect 42");
        }
        try {
            long id = Long.parseLong(args.trim());
            return decisionService.findById(id)
                    .map(d -> {
                        conversationService.reset();
                        conversationService.setState(ConversationState.WAITING_ACTUAL_RESULT);
                        conversationService.setPendingReflectId(id);
                        return text(chatId, String.format(
                                "Рефлексия к решению #%d\n\n_%s_\n\n*Шаг 1/2*: Что получилось по факту?",
                                id, truncate(d.getDecision(), 200)
                        ));
                    })
                    .orElse(text(chatId, "Решение #" + id + " не найдено."));
        } catch (NumberFormatException e) {
            return text(chatId, "Некорректный id. Пример: /reflect 42");
        }
    }

    public SendMessage handleInput(long chatId, String input) {
        return switch (conversationService.getState()) {
            case WAITING_ACTUAL_RESULT -> {
                conversationService.putBuffer("actualResult", input);
                conversationService.setState(ConversationState.WAITING_LESSON);
                yield text(chatId, "*Шаг 2/2*: Чему вы научились? (или отправьте \"-\" чтобы пропустить)");
            }
            case WAITING_LESSON -> {
                String lesson = "-".equals(input.trim()) ? "" : input;
                Long id = conversationService.getPendingReflectId();
                var updated = decisionService.addReflection(
                        id,
                        conversationService.getBuffer("actualResult"),
                        lesson
                );
                conversationService.reset();
                yield updated
                        .map(d -> text(chatId, "✅ Рефлексия сохранена!\n\n" + formatter.formatFull(d)))
                        .orElse(text(chatId, "Не удалось найти решение. Попробуйте ещё раз."));
            }
            default -> text(chatId, "Что-то пошло не так. Начните заново: /reflect [id]");
        };
    }

    private String truncate(String text, int maxLen) {
        if (text == null) return "";
        return text.length() <= maxLen ? text : text.substring(0, maxLen) + "…";
    }

    private SendMessage text(long chatId, String text) {
        SendMessage msg = new SendMessage();
        msg.setChatId(chatId);
        msg.setText(text);
        msg.setParseMode("Markdown");
        return msg;
    }
}
