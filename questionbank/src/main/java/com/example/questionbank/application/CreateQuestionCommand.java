package com.example.questionbank.application;

import com.example.questionbank.domain.model.Difficulty;
import com.example.questionbank.domain.model.QuestionMetadata;
import com.example.questionbank.domain.model.QuestionType;

import java.util.List;
import java.util.Set;

/**
 * Input data required to create a question.
 *
 * @param statement question wording
 * @param authorId identity of the author
 * @param topicId identity of the topic
 * @param competencyIds optional competencies assigned at creation time
 * @param type concrete question type
 * @param difficulty cognitive difficulty
 * @param options options of a multiple-choice question, ignored otherwise
 * @param expectedAnswer expected truth value of a true/false question
 * @param metadata optional editorial metadata
 */
public record CreateQuestionCommand(
        String statement,
        Long authorId,
        Long topicId,
        Set<Long> competencyIds,
        QuestionType type,
        Difficulty difficulty,
        List<OptionData> options,
        Boolean expectedAnswer,
        QuestionMetadata metadata) {

    /**
     * One option supplied when creating a multiple-choice question.
     *
     * @param text visible option text
     * @param correct whether it is the correct option
     */
    public record OptionData(String text, boolean correct) {
    }
}
