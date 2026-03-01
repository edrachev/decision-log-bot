package com.decisionlog.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class BotConfig {

    @Value("${telegram.bot.token}")
    private String token;

    @Value("${telegram.bot.username}")
    private String username;

    @Value("${telegram.bot.webhook-url}")
    private String webhookUrl;

    @Value("${telegram.bot.allowed-user-id}")
    private long allowedUserId;
}
