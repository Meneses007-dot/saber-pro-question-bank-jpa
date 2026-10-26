package com.example.questionbank.domain.model;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Lifecycle states of a question and the transitions allowed between them.
 *
 * <p>The state machine lives in the domain so that no caller can move a
 * question to an arbitrary state. Rejection returns the question to
 * {@link #DRAFT} so the author can correct it.</p>
 */
public enum QuestionStatus {
    /** Work in progress, not yet submitted. */
    DRAFT,
    /** Waiting for a reviewer to pick it up. */
    PENDING_REVIEW,
    /** Currently being evaluated by a reviewer. */
    IN_REVIEW,
    /** Approved but not yet visible to students. */
    APPROVED,
    /** Available for quizzes and simulations. */
    PUBLISHED,
    /** Retired from active use. */
    ARCHIVED;

    private static final Map<QuestionStatus, Set<QuestionStatus>> ALLOWED_TRANSITIONS;

    static {
        Map<QuestionStatus, Set<QuestionStatus>> transitions = new EnumMap<>(QuestionStatus.class);
        transitions.put(DRAFT, EnumSet.of(PENDING_REVIEW));
        transitions.put(PENDING_REVIEW, EnumSet.of(IN_REVIEW, DRAFT));
        transitions.put(IN_REVIEW, EnumSet.of(APPROVED, DRAFT));
        transitions.put(APPROVED, EnumSet.of(PUBLISHED, ARCHIVED));
        transitions.put(PUBLISHED, EnumSet.of(ARCHIVED));
        transitions.put(ARCHIVED, EnumSet.noneOf(QuestionStatus.class));
        ALLOWED_TRANSITIONS = Collections.unmodifiableMap(transitions);
    }

    /**
     * Indicates whether this state can move directly to the target state.
     *
     * @param target target state
     * @return {@code true} when the transition is part of the lifecycle
     */
    public boolean canTransitionTo(QuestionStatus target) {
        return target != null && ALLOWED_TRANSITIONS.get(this).contains(target);
    }

    /**
     * Returns the states reachable from this one.
     *
     * @return immutable set of allowed target states
     */
    public Set<QuestionStatus> allowedTransitions() {
        return Collections.unmodifiableSet(ALLOWED_TRANSITIONS.get(this));
    }
}
