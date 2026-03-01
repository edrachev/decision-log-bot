package com.decisionlog.service;

import com.decisionlog.domain.ConversationState;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Управляет состоянием многошаговых диалогов.
 * Хранит состояние в памяти — бот для одного пользователя.
 */
@Service
public class ConversationService {

    private ConversationState state = ConversationState.IDLE;

    // Временное хранилище данных текущего диалога /add
    private final Map<String, String> buffer = new HashMap<>();

    // id решения для текущего диалога /reflect
    private Long pendingReflectId = null;

    public ConversationState getState() {
        return state;
    }

    public void setState(ConversationState state) {
        this.state = state;
    }

    public void putBuffer(String key, String value) {
        buffer.put(key, value);
    }

    public String getBuffer(String key) {
        return buffer.get(key);
    }

    public void setPendingReflectId(Long id) {
        this.pendingReflectId = id;
    }

    public Long getPendingReflectId() {
        return pendingReflectId;
    }

    public void reset() {
        state = ConversationState.IDLE;
        buffer.clear();
        pendingReflectId = null;
    }
}
