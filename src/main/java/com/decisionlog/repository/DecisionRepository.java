package com.decisionlog.repository;

import com.decisionlog.domain.Decision;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DecisionRepository extends JpaRepository<Decision, Long> {

    List<Decision> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
