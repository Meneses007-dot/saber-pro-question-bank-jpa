package com.example.questionbank.domain.exception;

/**
 * Signals an attempt to move a question to a state that is not reachable from
 * its current state.
 */
public class InvalidStateTransitionException extends DomainException {

    /**
     * Creates the exception with an explanatory message.
     *
     * @param message description of the rejected transition
     */
    public InvalidStateTransitionException(String message) {
        super(message);
    }
}
