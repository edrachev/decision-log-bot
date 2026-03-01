package com.decisionlog.bot;

import com.decisionlog.config.BotConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebhookRegistrar implements ApplicationRunner {

    private final DecisionLogBot bot;
    private final BotConfig config;

    @Override
    public void run(ApplicationArguments args) {
        try {
            SetWebhook setWebhook = SetWebhook.builder()
                    .url(config.getWebhookUrl())
                    .build();
            bot.execute(setWebhook);
            log.info("Webhook registered: {}", config.getWebhookUrl());
        } catch (TelegramApiException e) {
            log.error("Failed to register webhook: {}", e.getMessage());
        }
    }
}
