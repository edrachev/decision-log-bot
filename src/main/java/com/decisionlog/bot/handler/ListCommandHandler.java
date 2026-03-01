package com.decisionlog.bot.handler;

import com.decisionlog.service.DecisionService;
import com.decisionlog.service.MessageFormatter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@Component
@RequiredArgsConstructor
public class ListCommandHandler {

    private final DecisionService decisionService;
    private final MessageFormatter formatter;

    public SendMessage handle(long chatId) {
        var decisions = decisionService.getRecent(10);
        SendMessage msg = new SendMessage();
        msg.setChatId(chatId);
        msg.setText(formatter.formatList(decisions));
        msg.setParseMode("Markdown");
        return msg;
    }
}
