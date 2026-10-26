package com.example.questionbank.infrastructure.jpa.record;

import com.example.questionbank.domain.model.Difficulty;
import com.example.questionbank.domain.model.QuestionStatus;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * JPA record for true/false questions.
 */
@Entity
@Table(name = "true_false_questions")
@DiscriminatorValue("TRUE_FALSE")
public class TrueFalseQuestionRecord extends QuestionRecord {

    @Column(name = "expected_answer", nullable = false)
    private Boolean expectedAnswer;

    /** Required by JPA. */
    protected TrueFalseQuestionRecord() {
    }

    /**
     * Creates a true/false question record.
     *
     * @param statement question wording
     * @param difficulty cognitive difficulty
     * @param status lifecycle state
     * @param author author record
     * @param topic topic record
     * @param expectedAnswer expected truth value
     */
    public TrueFalseQuestionRecord(String statement, Difficulty difficulty, QuestionStatus status,
                                   UserRecord author, TopicRecord topic, Boolean expectedAnswer) {
        super(statement, difficulty, status, author, topic);
        this.expectedAnswer = expectedAnswer;
    }

    /**
     * Updates the expected truth value.
     *
     * @param expectedAnswer expected truth value
     */
    public void updateExpectedAnswer(Boolean expectedAnswer) {
        this.expectedAnswer = expectedAnswer;
    }

    /** @return expected truth value */
    public Boolean getExpectedAnswer() {
        return expectedAnswer;
    }
}
