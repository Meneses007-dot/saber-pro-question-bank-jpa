package com.example.questionbank.api;

import com.example.questionbank.application.CreateQuizCommand;

import java.util.List;

/**
 * Request body used to create a quiz.
 *
 * @param title quiz title
 * @param createdById identity of the teacher that creates the quiz
 * @param questionIds identities of the selected questions
 */
public record QuizRequest(String title, Long createdById, List<Long> questionIds) {

    /**
     * Converts the HTTP request into an application command.
     *
     * @return creation command
     */
    public CreateQuizCommand toCommand() {
        return new CreateQuizCommand(title, createdById, questionIds);
    }
}
