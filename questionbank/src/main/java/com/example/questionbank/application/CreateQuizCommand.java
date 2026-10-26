package com.example.questionbank.application;

import java.util.List;

/**
 * Input data required to create a quiz.
 *
 * @param title quiz title
 * @param createdById identity of the user that creates the quiz
 * @param questionIds identities of the selected questions
 */
public record CreateQuizCommand(String title, Long createdById, List<Long> questionIds) {
}
