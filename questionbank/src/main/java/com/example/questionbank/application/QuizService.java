package com.example.questionbank.application;

import com.example.questionbank.domain.exception.DomainException;
import com.example.questionbank.domain.exception.ResourceNotFoundException;
import com.example.questionbank.domain.model.Question;
import com.example.questionbank.domain.model.Quiz;
import com.example.questionbank.domain.repository.QuestionRepository;
import com.example.questionbank.domain.repository.QuizRepository;
import com.example.questionbank.domain.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Application use cases for quizzes.
 */
@Service
@Transactional
public class QuizService {

    private final QuizRepository quizzes;
    private final QuestionRepository questions;
    private final UserRepository users;

    /**
     * Injects the domain repository contracts.
     *
     * @param quizzes quiz repository
     * @param questions question repository
     * @param users user repository
     */
    public QuizService(QuizRepository quizzes, QuestionRepository questions, UserRepository users) {
        this.quizzes = quizzes;
        this.questions = questions;
        this.users = users;
    }

    /**
     * Creates a quiz from published questions.
     *
     * <p>Every question is loaded and validated before the quiz is persisted,
     * so an invalid selection leaves the database unchanged.</p>
     *
     * @param command creation data
     * @return the persisted quiz
     * @throws DomainException when a question is not published
     * @throws ResourceNotFoundException when the creator or a question does not exist
     */
    public Quiz createQuiz(CreateQuizCommand command) {
        if (command.createdById() == null) {
            throw new DomainException("The quiz creator is required");
        }
        if (command.questionIds() == null || command.questionIds().isEmpty()) {
            throw new DomainException("A quiz must contain at least one question");
        }
        users.findById(command.createdById())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + command.createdById()));

        Set<Long> uniqueIds = new LinkedHashSet<>(command.questionIds());
        List<Question> selected = new ArrayList<>();
        for (Long questionId : uniqueIds) {
            selected.add(questions.findById(questionId)
                    .orElseThrow(() -> new ResourceNotFoundException("Question not found: " + questionId)));
        }
        Quiz quiz = Quiz.create(command.title(), command.createdById(), selected);
        return quizzes.save(quiz);
    }

    /**
     * Finds a quiz or fails.
     *
     * @param id quiz identity
     * @return the quiz
     * @throws ResourceNotFoundException when the quiz does not exist
     */
    @Transactional(readOnly = true)
    public Quiz getQuiz(Long id) {
        return quizzes.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found: " + id));
    }

    /**
     * Lists every quiz.
     *
     * @return all quizzes
     */
    @Transactional(readOnly = true)
    public List<Quiz> listQuizzes() {
        return quizzes.findAll();
    }
}
