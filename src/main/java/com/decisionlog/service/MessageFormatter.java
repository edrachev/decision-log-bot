package com.decisionlog.service;

import com.decisionlog.domain.Decision;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class MessageFormatter {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public String formatShort(Decision d) {
        String reflection = d.hasReflection() ? " ✅" : "";
        return String.format("#%d [%s]%s — %s",
                d.getId(),
                d.getCreatedAt().format(DATE_FMT),
                reflection,
                truncate(d.getDecision(), 80));
    }

    public String formatFull(Decision d) {
        StringBuilder sb = new StringBuilder();
        sb.append("*Решение #").append(d.getId()).append("*\n");
        sb.append("📅 ").append(d.getCreatedAt().format(DATE_FMT)).append("\n\n");
        sb.append("*Контекст:*\n").append(d.getContext()).append("\n\n");
        sb.append("*Решение:*\n").append(d.getDecision()).append("\n\n");
        sb.append("*Ожидаемый результат:*\n").append(d.getExpectedResult());

        if (d.hasReflection()) {
            sb.append("\n\n─────────────────\n");
            sb.append("*Рефлексия* (").append(d.getReflectedAt().format(DATE_FMT)).append(")\n\n");
            sb.append("*Что получилось:*\n").append(d.getActualResult());
            if (d.getLesson() != null && !d.getLesson().isBlank()) {
                sb.append("\n\n*Урок:*\n").append(d.getLesson());
            }
        } else {
            sb.append("\n\n_Рефлексия ещё не добавлена. /reflect ").append(d.getId()).append("_");
        }

        return sb.toString();
    }

    public String formatList(List<Decision> decisions) {
        if (decisions.isEmpty()) {
            return "Решений пока нет. Добавьте первое с помощью /add";
        }
        StringBuilder sb = new StringBuilder("*Последние решения:*\n\n");
        for (Decision d : decisions) {
            sb.append(formatShort(d)).append("\n");
        }
        sb.append("\nДля просмотра: /view \\[id\\]");
        return sb.toString();
    }

    private String truncate(String text, int maxLen) {
        if (text == null) return "";
        return text.length() <= maxLen ? text : text.substring(0, maxLen) + "…";
    }
}
