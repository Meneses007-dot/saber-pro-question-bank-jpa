package com.example.questionbank.support;

import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Deletes every row of the test database in foreign-key order.
 *
 * <p>The JPA contract tests inherit their test methods from an abstract class,
 * so Spring does not start a test transaction for them and each repository call
 * commits. Cleaning before every test keeps them isolated.</p>
 */
public final class DatabaseCleaner {

    private DatabaseCleaner() {
    }

    /**
     * Removes all rows from the application tables.
     *
     * @param jdbcTemplate template connected to the test database
     */
    public static void clean(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.update("delete from quiz_questions");
        jdbcTemplate.update("delete from quizzes");
        jdbcTemplate.update("delete from question_metadata");
        jdbcTemplate.update("delete from answer_options");
        jdbcTemplate.update("delete from question_competencies");
        jdbcTemplate.update("delete from multiple_choice_questions");
        jdbcTemplate.update("delete from true_false_questions");
        jdbcTemplate.update("delete from questions");
        jdbcTemplate.update("delete from users");
        jdbcTemplate.update("delete from topics");
        jdbcTemplate.update("delete from competencies");
    }
}
