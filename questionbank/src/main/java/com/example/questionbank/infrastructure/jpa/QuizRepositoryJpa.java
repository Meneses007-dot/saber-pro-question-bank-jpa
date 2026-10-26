package com.example.questionbank.infrastructure.jpa;

import com.example.questionbank.domain.exception.ResourceNotFoundException;
import com.example.questionbank.domain.model.Quiz;
import com.example.questionbank.domain.repository.QuizRepository;
import com.example.questionbank.infrastructure.jpa.mapper.QuizMapper;
import com.example.questionbank.infrastructure.jpa.record.QuestionRecord;
import com.example.questionbank.infrastructure.jpa.record.QuizRecord;
import com.example.questionbank.infrastructure.jpa.record.UserRecord;
import com.example.questionbank.infrastructure.jpa.repository.SpringQuestionDataRepository;
import com.example.questionbank.infrastructure.jpa.repository.SpringQuizDataRepository;
import com.example.questionbank.infrastructure.jpa.repository.SpringUserDataRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * JPA implementation of the {@link QuizRepository} contract.
 */
@Repository
public class QuizRepositoryJpa implements QuizRepository {

    private final SpringQuizDataRepository quizzes;
    private final SpringQuestionDataRepository questions;
    private final SpringUserDataRepository users;

    /**
     * Creates the adapter.
     *
     * @param quizzes Spring Data quiz repository
     * @param questions Spring Data question repository
     * @param users Spring Data user repository
     */
    public QuizRepositoryJpa(SpringQuizDataRepository quizzes, SpringQuestionDataRepository questions,
                             SpringUserDataRepository users) {
        this.quizzes = quizzes;
        this.questions = questions;
        this.users = users;
    }

    @Override
    @Transactional
    public Quiz save(Quiz quiz) {
        UserRecord creator = users.findById(quiz.getCreatedById())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found: " + quiz.getCreatedById()));
        Set<QuestionRecord> questionRecords = new LinkedHashSet<>(questions.findAllById(quiz.getQuestionIds()));
        if (questionRecords.size() != quiz.getQuestionIds().size()) {
            throw new ResourceNotFoundException(
                    "One or more questions were not found: " + quiz.getQuestionIds());
        }

        QuizRecord record;
        if (quiz.getId() == null) {
            record = QuizMapper.toNewRecord(quiz, creator, questionRecords);
        } else {
            record = quizzes.findById(quiz.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Quiz not found: " + quiz.getId()));
            QuizMapper.updateRecord(record, quiz, creator, questionRecords);
        }
        return QuizMapper.toDomain(quizzes.save(record));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Quiz> findById(Long id) {
        return quizzes.findById(id).map(QuizMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Quiz> findAll() {
        return quizzes.findAll().stream().map(QuizMapper::toDomain).toList();
    }
}
