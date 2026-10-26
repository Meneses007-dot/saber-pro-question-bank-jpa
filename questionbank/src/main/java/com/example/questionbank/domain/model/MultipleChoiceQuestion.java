package com.example.questionbank.domain.model;

import com.example.questionbank.domain.exception.DomainException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Question whose answer is chosen from a fixed set of options.
 *
 * <p>The academic rule (four distractors plus one correct answer) is enforced
 * here, when the question is submitted for review, and not by JPA.</p>
 */
public class MultipleChoiceQuestion extends Question {

    /** Number of options required by the academic rule. */
    public static final int REQUIRED_OPTIONS = 5;

    private final List<AnswerOption> options;

    /**
     * Creates or rehydrates a multiple-choice question.
     *
     * @param id identity assigned by the repository, or {@code null}
     * @param statement question wording
     * @param difficulty cognitive difficulty
     * @param status current lifecycle state
     * @param authorId identity of the author
     * @param topicId identity of the topic
     * @param competencyIds identities of the evaluated competencies
     * @param metadata optional editorial metadata
     * @param options answer options, may be empty while the question is a draft
     */
    public MultipleChoiceQuestion(Long id, String statement, Difficulty difficulty, QuestionStatus status,
                                  Long authorId, Long topicId, Collection<Long> competencyIds,
                                  QuestionMetadata metadata, Collection<AnswerOption> options) {
        super(id, statement, difficulty, status, authorId, topicId, competencyIds, metadata);
        this.options = new ArrayList<>(options == null ? List.of() : options);
    }

    /**
     * Adds an option to the question.
     *
     * @param text visible option text
     * @param correct whether this is the correct option
     */
    public void addOption(String text, boolean correct) {
        options.add(new AnswerOption(text, correct));
    }

    @Override
    protected void validateTypeSpecificRules() {
        if (options.size() != REQUIRED_OPTIONS) {
            throw new DomainException("A multiple-choice question must have exactly "
                    + REQUIRED_OPTIONS + " options; found " + options.size());
        }
        long correctOptions = options.stream().filter(AnswerOption::isCorrect).count();
        if (correctOptions != 1) {
            throw new DomainException("A multiple-choice question must have exactly one correct option; found "
                    + correctOptions);
        }
    }

    /** @return immutable view of the answer options */
    public List<AnswerOption> getOptions() {
        return List.copyOf(options);
    }
}
