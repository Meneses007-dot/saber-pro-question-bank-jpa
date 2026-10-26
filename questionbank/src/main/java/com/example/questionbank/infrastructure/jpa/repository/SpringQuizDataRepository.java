package com.example.questionbank.infrastructure.jpa.repository;

import com.example.questionbank.infrastructure.jpa.record.QuizRecord;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data repository used internally by the quiz repository adapter.
 */
public interface SpringQuizDataRepository extends JpaRepository<QuizRecord, Long> {
}
