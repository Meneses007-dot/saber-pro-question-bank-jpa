package com.example.questionbank.domain.exception;

/**
 * Signals that a required domain object does not exist.
 */
public class ResourceNotFoundException extends DomainException {

    /**
     * Creates the exception with an explanatory message.
     *
     * @param message identification of the missing resource
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
