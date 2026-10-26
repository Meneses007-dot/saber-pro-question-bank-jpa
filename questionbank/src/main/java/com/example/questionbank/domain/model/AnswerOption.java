package com.example.questionbank.domain.model;

import com.example.questionbank.domain.exception.DomainException;

/**
 * One option of a multiple-choice question.
 *
 * <p>Options are entities inside the question aggregate: they only make sense
 * as part of their question and are removed with it.</p>
 */
public class AnswerOption {

    private final Long id;
    private final String text;
    private final boolean correct;

    /**
     * Creates an option that does not have an identity yet.
     *
     * @param text visible option text
     * @param correct whether this is the correct option
     */
    public AnswerOption(String text, boolean correct) {
        this(null, text, correct);
    }

    /**
     * Creates an option, optionally with its persistent identity.
     *
     * @param id identity assigned by the repository, or {@code null}
     * @param text visible option text
     * @param correct whether this is the correct option
     */
    public AnswerOption(Long id, String text, boolean correct) {
        if (text == null || text.isBlank()) {
            throw new DomainException("The answer option text is required");
        }
        this.id = id;
        this.text = text.trim();
        this.correct = correct;
    }

    /** @return identity assigned by the repository, or {@code null} */
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
}
