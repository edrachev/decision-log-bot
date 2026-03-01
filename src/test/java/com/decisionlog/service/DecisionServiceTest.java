package com.decisionlog.service;

import com.decisionlog.domain.Decision;
import com.decisionlog.repository.DecisionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DecisionServiceTest {

    @Mock
    private DecisionRepository repository;

    @InjectMocks
    private DecisionService service;

    private Decision sampleDecision;

    @BeforeEach
    void setUp() {
        sampleDecision = new Decision();
        sampleDecision.setContext("Нужно выбрать архитектуру");
        sampleDecision.setDecision("Выбираем микросервисы");
        sampleDecision.setExpectedResult("Независимое масштабирование");
        // Эмулируем @PrePersist
        try {
            var method = Decision.class.getDeclaredMethod("onCreate");
            method.setAccessible(true);
            method.invoke(sampleDecision);
        } catch (Exception ignored) {}
    }

    @Test
    void save_shouldPersistDecisionWithAllFields() {
        when(repository.save(any(Decision.class))).thenAnswer(inv -> {
            Decision d = inv.getArgument(0);
            d.setContext("ctx");
            return d;
        });

        service.save("ctx", "решение", "результат");

        ArgumentCaptor<Decision> captor = ArgumentCaptor.forClass(Decision.class);
        verify(repository).save(captor.capture());
        Decision saved = captor.getValue();

        assertThat(saved.getContext()).isEqualTo("ctx");
        assertThat(saved.getDecision()).isEqualTo("решение");
        assertThat(saved.getExpectedResult()).isEqualTo("результат");
    }

    @Test
    void addReflection_shouldUpdateDecisionAndSetTimestamp() {
        sampleDecision.setContext("ctx");
        when(repository.findById(1L)).thenReturn(Optional.of(sampleDecision));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Optional<Decision> result = service.addReflection(1L, "Получилось хорошо", "Надо планировать");

        assertThat(result).isPresent();
        assertThat(result.get().getActualResult()).isEqualTo("Получилось хорошо");
        assertThat(result.get().getLesson()).isEqualTo("Надо планировать");
        assertThat(result.get().getReflectedAt()).isNotNull();
    }

    @Test
    void addReflection_shouldReturnEmptyForNonExistentId() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        Optional<Decision> result = service.addReflection(99L, "что-то", "урок");

        assertThat(result).isEmpty();
        verify(repository, never()).save(any());
    }

    @Test
    void getRecent_shouldRequestCorrectPageSize() {
        when(repository.findAllByOrderByCreatedAtDesc(any())).thenReturn(List.of());

        service.getRecent(10);

        verify(repository).findAllByOrderByCreatedAtDesc(PageRequest.of(0, 10));
    }

    @Test
    void findById_shouldDelegateToRepository() {
        when(repository.findById(1L)).thenReturn(Optional.of(sampleDecision));

        Optional<Decision> result = service.findById(1L);

        assertThat(result).isPresent();
    }
}
