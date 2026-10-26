package com.example.questionbank.infrastructure.jpa.record;

import com.example.questionbank.domain.model.Difficulty;
import com.example.questionbank.domain.model.QuestionStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

/**
 * JPA record for multiple-choice questions.
 *
 * <p>The inverse side of the one-to-many association keeps the options
 * together with their question; deleting the question deletes its options.</p>
 */
@Entity
@Table(name = "multiple_choice_questions")
@DiscriminatorValue("MULTIPLE_CHOICE")
public class MultipleChoiceQuestionRecord extends QuestionRecord {

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<AnswerOptionRecord> options = new ArrayList<>();

    /** Required by JPA. */
    protected MultipleChoiceQuestionRecord() {
    }

    /**
     * Creates a multiple-choice question record.
     *
     * @param statement question wording
     * @param difficulty cognitive difficulty
     * @param status lifecycle state
     * @param author author record
     * @param topic topic record
     */
    public MultipleChoiceQuestionRecord(String statement, Difficulty difficulty, QuestionStatus status,
                                        UserRecord author, TopicRecord topic) {
        super(statement, difficulty, status, author, topic);
    }

    /**
     * Adds an option and maintains both sides of the association.
     *
     * @param option option record
     */
    public void addOption(AnswerOptionRecord option) {
        options.add(option);
        option.setQuestion(this);
    }

    /** @return answer options */
    public List<AnswerOptionRecord> getOptions() {
        return options;
    }
}
