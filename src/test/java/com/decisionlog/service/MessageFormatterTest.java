package com.decisionlog.service;

import com.decisionlog.domain.Decision;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MessageFormatterTest {

    private MessageFormatter formatter;

    @BeforeEach
    void setUp() {
        formatter = new MessageFormatter();
    }

    private Decision decision(Long id, String decisionText) {
        Decision d = new Decision();
        d.setContext("Нужно выбрать стек");
        d.setDecision(decisionText);
        d.setExpectedResult("Быстрый старт");
        setId(d, id);
        setCreatedAt(d, LocalDateTime.of(2026, 3, 1, 10, 0));
        return d;
    }

    @Test
    void formatShort_withoutReflection_noCheckmark() {
        Decision d = decision(1L, "Выбрать Spring Boot");

        String result = formatter.formatShort(d);

        assertThat(result).contains("#1");
        assertThat(result).contains("Выбрать Spring Boot");
        assertThat(result).doesNotContain("✅");
    }

    @Test
    void formatShort_withReflection_hasCheckmark() {
        Decision d = decision(2L, "Выбрать Spring Boot");
        d.setActualResult("Всё получилось");

        String result = formatter.formatShort(d);

        assertThat(result).contains("✅");
    }

    @Test
    void formatShort_longDecision_truncated() {
        String longText = "A".repeat(200);
        Decision d = decision(1L, longText);

        String result = formatter.formatShort(d);

        assertThat(result).contains("…");
        assertThat(result.length()).isLessThan(longText.length());
    }

    @Test
    void formatFull_withoutReflection_hasReflectHint() {
        Decision d = decision(5L, "Мигрировать на микросервисы");

        String result = formatter.formatFull(d);

        assertThat(result).contains("Решение #5");
        assertThat(result).contains("Контекст:");
        assertThat(result).contains("Решение:");
        assertThat(result).contains("Ожидаемый результат:");
        assertThat(result).contains("/reflect 5");
    }

    @Test
    void formatFull_withReflection_showsReflectionSection() {
        Decision d = decision(5L, "Мигрировать на микросервисы");
        d.setActualResult("Заняло вдвое дольше");
        d.setLesson("Нужно декомпозировать на этапы");
        setReflectedAt(d, LocalDateTime.of(2026, 4, 1, 12, 0));

        String result = formatter.formatFull(d);

        assertThat(result).contains("Рефлексия");
        assertThat(result).contains("Что получилось:");
        assertThat(result).contains("Заняло вдвое дольше");
        assertThat(result).contains("Урок:");
        assertThat(result).contains("Нужно декомпозировать на этапы");
        assertThat(result).doesNotContain("/reflect");
    }

    @Test
    void formatList_empty_returnsHint() {
        String result = formatter.formatList(List.of());

        assertThat(result).contains("/add");
    }

    @Test
    void formatList_withDecisions_showsAll() {
        List<Decision> decisions = List.of(
                decision(1L, "Первое решение"),
                decision(2L, "Второе решение")
        );

        String result = formatter.formatList(decisions);

        assertThat(result).contains("#1");
        assertThat(result).contains("#2");
        assertThat(result).contains("/view");
    }

    // Helpers для установки приватных полей через reflection
    private void setId(Decision d, Long id) {
        try {
            var f = Decision.class.getDeclaredField("id");
            f.setAccessible(true);
            f.set(d, id);
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    private void setCreatedAt(Decision d, LocalDateTime dt) {
        try {
            var f = Decision.class.getDeclaredField("createdAt");
            f.setAccessible(true);
            f.set(d, dt);
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    private void setReflectedAt(Decision d, LocalDateTime dt) {
        try {
            var f = Decision.class.getDeclaredField("reflectedAt");
            f.setAccessible(true);
            f.set(d, dt);
        } catch (Exception e) { throw new RuntimeException(e); }
    }
}
