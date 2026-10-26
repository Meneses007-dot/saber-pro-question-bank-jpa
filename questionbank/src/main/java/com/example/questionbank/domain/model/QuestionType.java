package com.example.questionbank.domain.model;

/**
 * Concrete question types supported by the bank.
 */
public enum QuestionType {
    /** A question with several answer options and one correct option. */
    MULTIPLE_CHOICE,
    /** A question whose answer is either true or false. */
    TRUE_FALSE
}
