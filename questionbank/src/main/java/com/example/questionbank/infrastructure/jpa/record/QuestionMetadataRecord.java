package com.example.questionbank.infrastructure.jpa.record;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;

/**
 * JPA record for the optional one-to-one metadata of a question.
 *
 * <p>This is the owning side: the foreign key {@code question_id} lives in
 * {@code question_metadata} and is unique, so a question cannot have two
 * metadata rows.</p>
 */
@Entity
@Table(name = "question_metadata")
public class QuestionMetadataRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false, unique = true)
    private QuestionRecord question;

    @Column(name = "last_reviewed_at")
    private LocalDate lastReviewedAt;

    @Column(name = "cognitive_level", length = 100)
    private String cognitiveLevel;

    @Column(name = "estimated_minutes")
    private Integer estimatedMinutes;

    @Column(name = "editorial_notes", length = 1000)
    private String editorialNotes;

    /** Required by JPA. */
    protected QuestionMetadataRecord() {
    }

    /**
     * Creates a metadata record for a question.
     *
     * @param question owning question
     * @param lastReviewedAt last editorial review date
     * @param cognitiveLevel cognitive level
     * @param estimatedMinutes expected response time in minutes
     * @param editorialNotes editorial observations
     */
    public QuestionMetadataRecord(QuestionRecord question, LocalDate lastReviewedAt, String cognitiveLevel,
                                  Integer estimatedMinutes, String editorialNotes) {
        this.question = question;
        this.lastReviewedAt = lastReviewedAt;
        this.cognitiveLevel = cognitiveLevel;
        this.estimatedMinutes = estimatedMinutes;
        this.editorialNotes = editorialNotes;
    }

    /**
     * Updates the metadata columns.
     *
     * @param lastReviewedAt last editorial review date
     * @param cognitiveLevel cognitive level
     * @param estimatedMinutes expected response time in minutes
     * @param editorialNotes editorial observations
     */
    public void update(LocalDate lastReviewedAt, String cognitiveLevel,
                       Integer estimatedMinutes, String editorialNotes) {
        this.lastReviewedAt = lastReviewedAt;
        this.cognitiveLevel = cognitiveLevel;
        this.estimatedMinutes = estimatedMinutes;
        this.editorialNotes = editorialNotes;
    }

    /** @return database identity */
    public Long getId() {
        return id;
    }

    /** @return owning question */
    public QuestionRecord getQuestion() {
        return question;
    }

    /**
     * Sets the owning side of the association.
     *
     * @param question owning question
     */
    public void setQuestion(QuestionRecord question) {
        this.question = question;
    }

    /** @return last editorial review date */
    public LocalDate getLastReviewedAt() {
        return lastReviewedAt;
    }

    /** @return cognitive level */
    public String getCognitiveLevel() {
        return cognitiveLevel;
    }

    /** @return expected response time in minutes */
    public Integer getEstimatedMinutes() {
        return estimatedMinutes;
    }

    /** @return editorial observations */
    public String getEditorialNotes() {
        return editorialNotes;
    }
}
