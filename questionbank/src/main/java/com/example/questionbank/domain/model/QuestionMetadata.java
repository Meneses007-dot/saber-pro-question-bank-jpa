package com.example.questionbank.domain.model;

import com.example.questionbank.domain.exception.DomainException;

import java.time.LocalDate;

/**
 * Complementary editorial information attached to a question.
 *
 * <p>This is a value object owned by the question aggregate: its identity in
 * the database belongs to the persistence model, not to the domain.</p>
 */
public class QuestionMetadata {

    private final LocalDate lastReviewedAt;
    private final String cognitiveLevel;
    private final Integer estimatedMinutes;
    private final String editorialNotes;

    /**
     * Creates the metadata, validating the estimated response time.
     *
     * @param lastReviewedAt date of the last editorial review, may be {@code null}
     * @param cognitiveLevel cognitive level of the question, may be {@code null}
     * @param estimatedMinutes expected response time in minutes, may be {@code null}
     * @param editorialNotes free-text observations, may be {@code null}
     */
    public QuestionMetadata(LocalDate lastReviewedAt, String cognitiveLevel,
                            Integer estimatedMinutes, String editorialNotes) {
        if (estimatedMinutes != null && estimatedMinutes <= 0) {
            throw new DomainException("The estimated response time must be positive");
        }
        this.lastReviewedAt = lastReviewedAt;
        this.cognitiveLevel = normalize(cognitiveLevel);
        this.estimatedMinutes = estimatedMinutes;
        this.editorialNotes = normalize(editorialNotes);
    }

    private static String normalize(String value) {
        return value == null ? null : value.trim();
    }

    /** @return date of the last editorial review, or {@code null} */
    public LocalDate getLastReviewedAt() {
        return lastReviewedAt;
    }

    /** @return cognitive level, or {@code null} */
    public String getCognitiveLevel() {
        return cognitiveLevel;
    }

    /** @return expected response time in minutes, or {@code null} */
    public Integer getEstimatedMinutes() {
        return estimatedMinutes;
    }

    /** @return editorial observations, or {@code null} */
    public String getEditorialNotes() {
        return editorialNotes;
    }
}
