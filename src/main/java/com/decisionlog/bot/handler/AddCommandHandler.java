package com.decisionlog.bot.handler;

import com.decisionlog.domain.ConversationState;
import com.decisionlog.domain.Decision;
import com.decisionlog.service.ConversationService;
import com.decisionlog.service.DecisionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@Component
@RequiredArgsConstructor
public class AddCommandHandler {

    private final DecisionService decisionService;
    private final ConversationService conversationService;

    public SendMessage handleCommand(long chatId) {
        conversationService.reset();
        conversationService.setState(ConversationState.WAITING_CONTEXT);
        return text(chatId, "Добавляем новое решение.\n\n*Шаг 1/3*: Опишите контекст — ситуацию, в которой нужно было принять решение.");
    }

    public SendMessage handleInput(long chatId, String input) {
        return switch (conversationService.getState()) {
            case WAITING_CONTEXT -> {
                conversationService.putBuffer("context", input);
                conversationService.setState(ConversationState.WAITING_DECISION);
                yield text(chatId, "*Шаг 2/3*: Напишите само решение — что именно вы решили сделать.");
            }
            case WAITING_DECISION -> {
                conversationService.putBuffer("decision", input);
                conversationService.setState(ConversationState.WAITING_EXPECTED_RESULT);
                yield text(chatId, "*Шаг 3/3*: Какой результат вы ожидаете от этого решения?");
            }
            case WAITING_EXPECTED_RESULT -> {
                conversationService.putBuffer("expectedResult", input);
                Decision saved = decisionService.save(
                        conversationService.getBuffer("context"),
                        conversationService.getBuffer("decision"),
                        input
                );
                conversationService.reset();
                yield text(chatId, String.format(
                        "✅ Решение #%d сохранено!\n\nДобавьте рефлексию позже командой /reflect %d",
                        saved.getId(), saved.getId()
                ));
            }
            default -> text(chatId, "Что-то пошло не так. Начните заново: /add");
        };
    }

    private SendMessage text(long chatId, String text) {
        SendMessage msg = new SendMessage();
        msg.setChatId(chatId);
        msg.setText(text);
        msg.setParseMode("Markdown");
        return msg;
    }
}
