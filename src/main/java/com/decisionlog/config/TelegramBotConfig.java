package com.decisionlog.config;

import com.decisionlog.bot.DecisionLogBot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.BotSession;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

/**
 * Ручная регистрация бота для polling.
 * telegrambots-spring-boot-starter 6.x использует spring.factories,
 * который не поддерживается в Spring Boot 3.x — поэтому регистрируем сами.
 */
@Slf4j
@Configuration
public class TelegramBotConfig {

    @Bean
    public TelegramBotsApi telegramBotsApi() throws TelegramApiException {
        return new TelegramBotsApi(DefaultBotSession.class);
    }

    @Bean
    public BotSession botSession(TelegramBotsApi api, DecisionLogBot bot) throws TelegramApiException {
        log.info("Registering Telegram bot: {}", bot.getBotUsername());
        return api.registerBot(bot);
    }
}
