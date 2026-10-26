package com.example.questionbank.api;

import com.example.questionbank.domain.model.AnswerOption;
import com.example.questionbank.domain.model.MultipleChoiceQuestion;
import com.example.questionbank.domain.model.Question;
import com.example.questionbank.domain.model.QuestionMetadata;
import com.example.questionbank.domain.model.QuestionStatus;
import com.example.questionbank.domain.model.Difficulty;
import com.example.questionbank.domain.model.TrueFalseQuestion;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * Response body returned for a question.
 *
 * @param id question identity
 * @param statement question wording
 * @param type concrete question type
 * @param difficulty cognitive difficulty
 * @param status lifecycle state
 * @param authorId identity of the author
 * @param topicId identity of the topic
 * @param competencyIds identities of the evaluated competencies
 * @param options options of a multiple-choice question
 * @param expectedAnswer expected truth value of a true/false question
 * @param metadata editorial metadata
 */
public record QuestionResponse(
        Long id,
        String statement,
        String type,
        Difficulty difficulty,
        QuestionStatus status,
        Long authorId,
        Long topicId,
        Set<Long> competencyIds,
        List<OptionResponse> options,
        Boolean expectedAnswer,
        MetadataResponse metadata) {

    /**
     * Builds a response from a domain question.
     *
     * @param question domain question
     * @return response body
     */
    public static QuestionResponse from(Question question) {
        List<OptionResponse> optionResponses = null;
        Boolean expectedAnswer = null;
        String type;
        if (question instanceof MultipleChoiceQuestion multipleChoice) {
            type = "MULTIPLE_CHOICE";
            optionResponses = multipleChoice.getOptions().stream()
                    .map(option -> new OptionResponse(option.getId(), option.getText(), option.isCorrect()))
                    .toList();
        } else if (question instanceof TrueFalseQuestion trueFalse) {
            type = "TRUE_FALSE";
            expectedAnswer = trueFalse.getExpectedAnswer();
        } else {
            type = question.getClass().getSimpleName();
        }
        return new QuestionResponse(question.getId(), question.getStatement(), type,
                question.getDifficulty(), question.getStatus(), question.getAuthorId(),
                question.getTopicId(), question.getCompetencyIds(), optionResponses,
                expectedAnswer, MetadataResponse.from(question.getMetadata()));
    }

    /**
     * Response representation of an answer option.
     *
     * @param id option identity
     * @param text visible text
     * @param correct whether it is the correct option
     */
    public record OptionResponse(Long id, String text, boolean correct) {
    }

    /**
     * Response representation of the editorial metadata.
     *
     * @param lastReviewedAt last editorial review date
     * @param cognitiveLevel cognitive level
     * @param estimatedMinutes expected response time in minutes
     * @param editorialNotes editorial observations
     */
    public record MetadataResponse(LocalDate lastReviewedAt, String cognitiveLevel,
                                   Integer estimatedMinutes, String editorialNotes) {

        /**
         * Builds the response from domain metadata.
         *
         * @param metadata domain metadata, may be {@code null}
         * @return response metadata, or {@code null}
         */
        public static MetadataResponse from(QuestionMetadata metadata) {
            if (metadata == null) {
                return null;
            }
            return new MetadataResponse(metadata.getLastReviewedAt(), metadata.getCognitiveLevel(),
                    metadata.getEstimatedMinutes(), metadata.getEditorialNotes());
        }
    }
}
