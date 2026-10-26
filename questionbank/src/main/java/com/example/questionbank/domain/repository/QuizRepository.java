package com.example.questionbank.domain.repository;

import com.example.questionbank.domain.model.Quiz;

import java.util.List;
import java.util.Optional;

/**
 * Persistence contract for quizzes.
 */
public interface QuizRepository {

    /**
     * Inserts or updates a quiz.
     *
     * @param quiz quiz to persist
     * @return the persisted quiz
     */
    Quiz save(Quiz quiz);

    /**
     * Finds a quiz by identity.
     *
     * @param id quiz identity
     * @return the quiz when it exists
     */
    Optional<Quiz> findById(Long id);

    /**
     * Lists every quiz.
     *
     * @return all quizzes
     */
    List<Quiz> findAll();
}
