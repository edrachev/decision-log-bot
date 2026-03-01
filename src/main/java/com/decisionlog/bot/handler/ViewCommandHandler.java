package com.decisionlog.bot.handler;

import com.decisionlog.service.DecisionService;
import com.decisionlog.service.MessageFormatter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@Component
@RequiredArgsConstructor
public class ViewCommandHandler {

    private final DecisionService decisionService;
    private final MessageFormatter formatter;

    public SendMessage handle(long chatId, String args) {
        if (args == null || args.isBlank()) {
            return text(chatId, "Укажите id решения: /view 42");
        }
        try {
            long id = Long.parseLong(args.trim());
            return decisionService.findById(id)
                    .map(d -> text(chatId, formatter.formatFull(d)))
                    .orElse(text(chatId, "Решение #" + id + " не найдено."));
        } catch (NumberFormatException e) {
            return text(chatId, "Некорректный id. Пример: /view 42");
        }
    }

    private SendMessage text(long chatId, String text) {
        SendMessage msg = new SendMessage();
        msg.setChatId(chatId);
        msg.setText(text);
        msg.setParseMode("Markdown");
        return msg;
    }
}
