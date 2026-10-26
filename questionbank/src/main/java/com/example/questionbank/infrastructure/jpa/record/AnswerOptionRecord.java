package com.example.questionbank.infrastructure.jpa.record;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * JPA record for one option of a multiple-choice question.
 *
 * <p>This is the owning side of the one-to-many association: every row stores
 * the foreign key {@code question_id}, which cannot be null.</p>
 */
@Entity
@Table(name = "answer_options")
public class AnswerOptionRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "option_text", nullable = false, length = 500)
    private String text;

    @Column(name = "is_correct", nullable = false)
    private boolean correct;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private MultipleChoiceQuestionRecord question;

    /** Required by JPA. */
    protected AnswerOptionRecord() {
    }

    /**
     * Creates an option record.
     *
     * @param text visible option text
     * @param correct whether it is the correct option
     */
    public AnswerOptionRecord(String text, boolean correct) {
        this.text = text;
        this.correct = correct;
    }

    /**
     * Updates the mutable columns of the record.
     *
     * @param text visible option text
     * @param correct whether it is the correct option
     */
    public void update(String text, boolean correct) {
        this.text = text;
        this.correct = correct;
    }

    /** @return database identity */
    public Long getId() {
        return id;
    }

    /** @return visible option text */
    public String getText() {
        return text;
    }

    /** @return {@code true} when this is the correct option */
    public boolean isCorrect() {
        return correct;
    }

    /** @return owning question */
    public MultipleChoiceQuestionRecord getQuestion() {
        return question;
    }

    /**
     * Sets the owning side of the association.
     *
     * @param question owning question
     */
    public void setQuestion(MultipleChoiceQuestionRecord question) {
        this.question = question;
    }
}
