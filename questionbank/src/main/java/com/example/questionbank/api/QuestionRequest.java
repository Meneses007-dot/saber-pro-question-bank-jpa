package com.example.questionbank.api;

import com.example.questionbank.application.CreateQuestionCommand;
import com.example.questionbank.domain.model.Difficulty;
import com.example.questionbank.domain.model.QuestionMetadata;
import com.example.questionbank.domain.model.QuestionType;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * Request body used to create a question.
 *
 * @param statement question wording
 * @param authorId identity of the author
 * @param topicId identity of the topic
 * @param competencyIds optional competencies
 * @param type concrete question type
 * @param difficulty cognitive difficulty
 * @param options options of a multiple-choice question
 * @param expectedAnswer expected truth value of a true/false question
 * @param metadata optional editorial metadata
 */
public record QuestionRequest(
        String statement,
        Long authorId,
        Long topicId,
        Set<Long> competencyIds,
        QuestionType type,
        Difficulty difficulty,
        List<OptionRequest> options,
        Boolean expectedAnswer,
        MetadataRequest metadata) {

    /**
     * Converts the HTTP request into an application command.
     *
     * @return creation command
     */
    public CreateQuestionCommand toCommand() {
        List<CreateQuestionCommand.OptionData> optionData = options == null ? null
                : options.stream()
                        .map(option -> new CreateQuestionCommand.OptionData(option.text(), option.correct()))
                        .toList();
        QuestionMetadata questionMetadata = metadata == null ? null
                : new QuestionMetadata(metadata.lastReviewedAt(), metadata.cognitiveLevel(),
                        metadata.estimatedMinutes(), metadata.editorialNotes());
        return new CreateQuestionCommand(statement, authorId, topicId, competencyIds, type, difficulty,
                optionData, expectedAnswer, questionMetadata);
    }

    /**
     * One option supplied in the request body.
     *
     * @param text visible option text
     * @param correct whether it is the correct option
     */
    public record OptionRequest(String text, boolean correct) {
    }

    /**
     * Optional metadata supplied in the request body.
     *
     * @param lastReviewedAt last editorial review date
     * @param cognitiveLevel cognitive level
     * @param estimatedMinutes expected response time in minutes
     * @param editorialNotes editorial observations
     */
    public record MetadataRequest(LocalDate lastReviewedAt, String cognitiveLevel,
                                  Integer estimatedMinutes, String editorialNotes) {
    }
}
