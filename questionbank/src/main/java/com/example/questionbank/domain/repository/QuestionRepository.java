package com.example.questionbank.domain.repository;

import com.example.questionbank.domain.model.Question;
import com.example.questionbank.domain.model.QuestionStatus;

import java.util.List;
import java.util.Optional;

/**
 * Persistence contract for questions, expressed without Spring or JPA types.
 */
public interface QuestionRepository {

    /**
     * Inserts or updates a question.
     *
     * @param question question to persist
     * @return the persisted question, including its generated identity
     */
    Question save(Question question);

    /**
     * Finds a question by identity.
     *
     * @param id question identity
     * @return the question when it exists
     */
    Optional<Question> findById(Long id);

    /**
     * Lists every question.
     *
     * @return all questions
     */
    List<Question> findAll();

    /**
     * Lists the questions created by an author.
     *
     * @param authorId author identity
     * @return questions of that author
     */
    List<Question> findByAuthorId(Long authorId);

    /**
     * Lists the questions in a given lifecycle state.
     *
     * @param status lifecycle state
     * @return questions in that state
     */
    List<Question> findByStatus(QuestionStatus status);

    /**
     * Lists the published questions that evaluate a competency.
     *
     * @param competencyId competency identity
     * @return published questions associated with the competency
     */
    List<Question> findPublishedByCompetencyId(Long competencyId);
}
