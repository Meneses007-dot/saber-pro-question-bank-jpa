package com.example.questionbank.domain.model;

/**
 * Roles recognized by the question bank.
 */
public enum Role {
    /** Manages users, roles and system parameters. */
    ADMIN,
    /** Creates and edits questions. */
    AUTHOR,
    /** Evaluates the quality of the questions. */
    REVIEWER,
    /** Organizes quizzes and simulations. */
    TEACHER,
    /** Answers questions and consults results. */
    STUDENT
}
