package com.example.questionbank.api;

import com.example.questionbank.domain.model.Quiz;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response body returned for a quiz.
 *
 * @param id quiz identity
 * @param title quiz title
 * @param createdById identity of the creator
 * @param questionIds identities of the selected questions
 * @param createdAt creation timestamp
 */
public record QuizResponse(Long id, String title, Long createdById, List<Long> questionIds,
                           LocalDateTime createdAt) {

    /**
     * Builds a response from a domain quiz.
     *
     * @param quiz domain quiz
     * @return response body
     */
    public static QuizResponse from(Quiz quiz) {
        return new QuizResponse(quiz.getId(), quiz.getTitle(), quiz.getCreatedById(),
                quiz.getQuestionIds(), quiz.getCreatedAt());
    }
}
