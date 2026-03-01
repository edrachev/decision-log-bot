package com.decisionlog.bot;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

@RestController
@RequiredArgsConstructor
public class WebhookController {

    private final DecisionLogBot bot;

    @PostMapping("/{botUsername}")
    public BotApiMethod<?> onUpdate(@PathVariable String botUsername,
                                    @RequestBody Update update) {
        return bot.onWebhookUpdateReceived(update);
    }
}
