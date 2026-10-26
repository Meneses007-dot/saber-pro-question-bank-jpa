package com.example.questionbank.domain.exception;

/**
 * Signals that a business rule of the question bank was violated.
 */
public class DomainException extends RuntimeException {

    /**
     * Creates the exception with an explanatory message.
     *
     * @param message description of the violated rule
     */
    public DomainException(String message) {
        super(message);
    }
}
