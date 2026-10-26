package com.example.questionbank.application;

import java.util.Set;

/**
 * Input data required to classify an existing question.
 *
 * @param topicId identity of the topic
 * @param competencyIds identities of the competencies to associate
 */
public record ClassifyQuestionCommand(Long topicId, Set<Long> competencyIds) {
}
