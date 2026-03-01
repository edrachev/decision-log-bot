package com.decisionlog.service;

import com.decisionlog.domain.ConversationState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConversationServiceTest {

    private ConversationService service;

    @BeforeEach
    void setUp() {
        service = new ConversationService();
    }

    @Test
    void initialState_shouldBeIdle() {
        assertThat(service.getState()).isEqualTo(ConversationState.IDLE);
    }

    @Test
    void setState_shouldUpdateState() {
        service.setState(ConversationState.WAITING_CONTEXT);
        assertThat(service.getState()).isEqualTo(ConversationState.WAITING_CONTEXT);
    }

    @Test
    void buffer_shouldStoreAndRetrieveValues() {
        service.putBuffer("context", "тестовый контекст");
        assertThat(service.getBuffer("context")).isEqualTo("тестовый контекст");
    }

    @Test
    void reset_shouldClearAllState() {
        service.setState(ConversationState.WAITING_DECISION);
        service.putBuffer("context", "что-то");
        service.setPendingReflectId(42L);

        service.reset();

        assertThat(service.getState()).isEqualTo(ConversationState.IDLE);
        assertThat(service.getBuffer("context")).isNull();
        assertThat(service.getPendingReflectId()).isNull();
    }

    @Test
    void pendingReflectId_shouldBeStoredAndCleared() {
        service.setPendingReflectId(7L);
        assertThat(service.getPendingReflectId()).isEqualTo(7L);

        service.reset();
        assertThat(service.getPendingReflectId()).isNull();
    }
}
