package com.decisionlog.service;

import com.decisionlog.domain.Decision;
import com.decisionlog.repository.DecisionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DecisionService {

    private final DecisionRepository repository;

    @Transactional
    public Decision save(String context, String decision, String expectedResult) {
        Decision entity = new Decision();
        entity.setContext(context);
        entity.setDecision(decision);
        entity.setExpectedResult(expectedResult);
        return repository.save(entity);
    }

    @Transactional
    public Optional<Decision> addReflection(Long id, String actualResult, String lesson) {
        return repository.findById(id).map(decision -> {
            decision.setActualResult(actualResult);
            decision.setLesson(lesson);
            decision.setReflectedAt(LocalDateTime.now());
            return repository.save(decision);
        });
    }

    @Transactional(readOnly = true)
    public List<Decision> getRecent(int limit) {
        return repository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, limit));
    }

    @Transactional(readOnly = true)
    public Optional<Decision> findById(Long id) {
        return repository.findById(id);
    }
}
