package com.example.questionbank.api;

import com.example.questionbank.application.ClassifyQuestionCommand;

import java.util.Set;

/**
 * Request body used to classify an existing question.
 *
 * @param topicId identity of the new topic
 * @param competencyIds identities of the competencies
 */
public record ClassifyQuestionRequest(Long topicId, Set<Long> competencyIds) {

    /**
     * Converts the HTTP request into an application command.
     *
     * @return classification command
     */
    public ClassifyQuestionCommand toCommand() {
        return new ClassifyQuestionCommand(topicId, competencyIds);
    }
}
