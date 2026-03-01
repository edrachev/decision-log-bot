# Decision Log Bot — Project Context

## Что это
Telegram бот для логирования управленческих решений и рефлексии по ним.
Личный инструмент CTO.

## Стек
- Java 21
- Spring Boot 3.x
- Spring Data JPA
- PostgreSQL (основное хранилище)
- TelegramBots library (rubenlagus/TelegramBots)
- Maven

## Архитектурные решения
- Polling режим (TelegramLongPollingBot) — бот на VPS, опрашивает TG API
- Один пользователь (я) — авторизация по telegram user_id из env
- Решения хранятся в SQLite (/data/decisionlog.db), не в памяти
- Конфиги только через env переменные, никаких секретов в коде

## Структура пакетов
com.decisionlog
  ├── bot/        — telegram handlers
  ├── domain/     — entities
  ├── service/    — бизнес логика
  ├── repository/ — JPA репозитории
  └── config/     — конфиги

## Что нельзя делать
- Не хранить секреты в application.properties
- Не писать бизнес-логику в handlers напрямую

## Definition of Done для фичи
- Код написан
- Unit тесты на сервисный слой
- Работает локально
- Dockerfile обновлён если нужно