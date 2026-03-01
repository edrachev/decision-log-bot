package com.decisionlog.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "decisions")
@Getter
@Setter
@NoArgsConstructor
public class Decision {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "decision_seq")
    @SequenceGenerator(name = "decision_seq", sequenceName = "decision_seq", allocationSize = 1)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String context;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String decision;

    @Column(name = "expected_result", nullable = false, columnDefinition = "TEXT")
    private String expectedResult;

    @Column(name = "actual_result", columnDefinition = "TEXT")
    private String actualResult;

    @Column(name = "lesson", columnDefinition = "TEXT")
    private String lesson;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "reflected_at")
    private LocalDateTime reflectedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public boolean hasReflection() {
        return actualResult != null && !actualResult.isBlank();
    }
}
