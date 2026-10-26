package com.example.questionbank.infrastructure.jpa.mapper;

import com.example.questionbank.domain.model.Quiz;
import com.example.questionbank.infrastructure.jpa.record.QuestionRecord;
import com.example.questionbank.infrastructure.jpa.record.QuizRecord;
import com.example.questionbank.infrastructure.jpa.record.UserRecord;

import java.util.Set;

/**
 * Translates quizzes between the domain and the JPA model.
 */
public final class QuizMapper {

    private QuizMapper() {
    }

    /**
     * Converts a JPA record to a domain quiz.
     *
     * @param record JPA record
     * @return domain quiz
     */
    public static Quiz toDomain(QuizRecord record) {
        return new Quiz(record.getId(), record.getTitle(), record.getCreatedBy().getId(),
                record.getQuestions().stream().map(QuestionRecord::getId).toList(),
                record.getCreatedAt());
    }

    /**
     * Creates a new JPA record from a domain quiz.
     *
     * @param quiz domain quiz
     * @param creator creator record
     * @param questions selected question records
     * @return new JPA record
     */
    public static QuizRecord toNewRecord(Quiz quiz, UserRecord creator, Set<QuestionRecord> questions) {
        QuizRecord record = new QuizRecord(quiz.getTitle(), creator, quiz.getCreatedAt());
        record.getQuestions().addAll(questions);
        return record;
    }

    /**
     * Copies the state of a domain quiz into an existing managed record.
     *
     * @param record managed record
     * @param quiz domain quiz
     * @param creator creator record
     * @param questions selected question records
     */
    public static void updateRecord(QuizRecord record, Quiz quiz, UserRecord creator,
                                    Set<QuestionRecord> questions) {
        record.update(quiz.getTitle(), creator);
        record.getQuestions().clear();
        record.getQuestions().addAll(questions);
    }
}
