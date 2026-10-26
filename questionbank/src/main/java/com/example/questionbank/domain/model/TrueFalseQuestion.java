package com.example.questionbank.domain.model;

import com.example.questionbank.domain.exception.DomainException;

import java.util.Collection;

/**
 * Question whose expected answer is either true or false.
 */
public class TrueFalseQuestion extends Question {

    private final Boolean expectedAnswer;

    /**
     * Creates or rehydrates a true/false question.
     *
     * @param id identity assigned by the repository, or {@code null}
     * @param statement question wording
     * @param difficulty cognitive difficulty
     * @param status current lifecycle state
     * @param authorId identity of the author
     * @param topicId identity of the topic
     * @param competencyIds identities of the evaluated competencies
     * @param metadata optional editorial metadata
     * @param expectedAnswer expected truth value, may be {@code null} while draft
     */
    public TrueFalseQuestion(Long id, String statement, Difficulty difficulty, QuestionStatus status,
                             Long authorId, Long topicId, Collection<Long> competencyIds,
                             QuestionMetadata metadata, Boolean expectedAnswer) {
        super(id, statement, difficulty, status, authorId, topicId, competencyIds, metadata);
        this.expectedAnswer = expectedAnswer;
    }

    @Override
    protected void validateTypeSpecificRules() {
        if (expectedAnswer == null) {
            throw new DomainException("A true/false question must define the expected answer");
        }
    }

    /** @return expected truth value, or {@code null} while draft */
    public Boolean getExpectedAnswer() {
        return expectedAnswer;
    }
}
