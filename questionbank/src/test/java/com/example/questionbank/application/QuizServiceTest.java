package com.example.questionbank.application;

import com.example.questionbank.domain.exception.DomainException;
import com.example.questionbank.domain.exception.ResourceNotFoundException;
import com.example.questionbank.domain.model.Competency;
import com.example.questionbank.domain.model.Difficulty;
import com.example.questionbank.domain.model.MultipleChoiceQuestion;
import com.example.questionbank.domain.model.Question;
import com.example.questionbank.domain.model.QuestionStatus;
import com.example.questionbank.domain.model.Quiz;
import com.example.questionbank.domain.model.Role;
import com.example.questionbank.domain.model.Topic;
import com.example.questionbank.domain.model.User;
import com.example.questionbank.support.InMemoryCompetencyRepository;
import com.example.questionbank.support.InMemoryQuestionRepository;
import com.example.questionbank.support.InMemoryQuizRepository;
import com.example.questionbank.support.InMemoryTopicRepository;
import com.example.questionbank.support.InMemoryUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests of the quiz use cases using in-memory repositories.
 */
class QuizServiceTest {

    private InMemoryQuestionRepository questions;
    private InMemoryQuizRepository quizzes;
    private InMemoryUserRepository users;
    private QuizService service;

    private Long teacherId;
    private Long authorId;
    private Long topicId;
    private Long competencyId;

    @BeforeEach
    void setUp() {
        questions = new InMemoryQuestionRepository();
        quizzes = new InMemoryQuizRepository();
        users = new InMemoryUserRepository();
        InMemoryTopicRepository topics = new InMemoryTopicRepository();
        InMemoryCompetencyRepository competencies = new InMemoryCompetencyRepository();
        service = new QuizService(quizzes, questions, users);

        teacherId = users.save(new User(null, "Tomas Teacher", "teacher@test.edu", Role.TEACHER)).getId();
        authorId = users.save(new User(null, "Arturo Author", "author@test.edu", Role.AUTHOR)).getId();
        topicId = topics.save(new Topic(null, "Software Architecture")).getId();
        competencyId = competencies.save(new Competency(null, "Solution Design", "Design")).getId();
    }

    private MultipleChoiceQuestion questionInStatus(QuestionStatus target) {
        MultipleChoiceQuestion question = new MultipleChoiceQuestion(null, "Which strategy is JOINED?",
                Difficulty.MEDIUM, QuestionStatus.DRAFT, authorId, topicId, Set.of(competencyId), null, null);
        for (int index = 1; index <= 5; index++) {
            question.addOption("Option " + index, index == 1);
        }
        if (target == QuestionStatus.PENDING_REVIEW || target == QuestionStatus.IN_REVIEW
                || target == QuestionStatus.APPROVED || target == QuestionStatus.PUBLISHED
                || target == QuestionStatus.ARCHIVED) {
            question.submitForReview();
        }
        if (target == QuestionStatus.IN_REVIEW || target == QuestionStatus.APPROVED
                || target == QuestionStatus.PUBLISHED || target == QuestionStatus.ARCHIVED) {
            question.startReview();
        }
        if (target == QuestionStatus.APPROVED || target == QuestionStatus.PUBLISHED
                || target == QuestionStatus.ARCHIVED) {
            question.approve();
        }
        if (target == QuestionStatus.PUBLISHED || target == QuestionStatus.ARCHIVED) {
            question.publish();
        }
        if (target == QuestionStatus.ARCHIVED) {
            question.archive();
        }
        questions.save(question);
        return question;
    }

    @Test
    void createsAQuizWithPublishedQuestions() {
        Question published = questionInStatus(QuestionStatus.PUBLISHED);

        Quiz quiz = service.createQuiz(new CreateQuizCommand("Simulation 1", teacherId,
                List.of(published.getId())));

        assertThat(quiz.getId()).isNotNull();
        assertThat(quiz.getQuestionIds()).containsExactly(published.getId());
        assertThat(quizzes.findAll()).hasSize(1);
    }

    @Test
    void rejectsADraftQuestion() {
        Question draft = questionInStatus(QuestionStatus.DRAFT);

        assertThatThrownBy(() -> service.createQuiz(new CreateQuizCommand("Simulation", teacherId,
                List.of(draft.getId()))))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("DRAFT")
                .hasMessageContaining("PUBLISHED");
    }

    @Test
    void rejectsAnApprovedQuestion() {
        Question approved = questionInStatus(QuestionStatus.APPROVED);

        assertThatThrownBy(() -> service.createQuiz(new CreateQuizCommand("Simulation", teacherId,
                List.of(approved.getId()))))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("APPROVED");
    }

    @Test
    void rejectsAnArchivedQuestion() {
        Question archived = questionInStatus(QuestionStatus.ARCHIVED);

        assertThatThrownBy(() -> service.createQuiz(new CreateQuizCommand("Simulation", teacherId,
                List.of(archived.getId()))))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("ARCHIVED");
    }

    @Test
    void persistsNothingWhenOneQuestionIsNotPublished() {
        Question published = questionInStatus(QuestionStatus.PUBLISHED);
        Question draft = questionInStatus(QuestionStatus.DRAFT);

        assertThatThrownBy(() -> service.createQuiz(new CreateQuizCommand("Mixed simulation", teacherId,
                List.of(published.getId(), draft.getId()))))
                .isInstanceOf(DomainException.class);
        assertThat(quizzes.findAll()).isEmpty();
    }

    @Test
    void rejectsAnUnknownQuestion() {
        assertThatThrownBy(() -> service.createQuiz(new CreateQuizCommand("Simulation", teacherId,
                List.of(999L))))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Question");
        assertThat(quizzes.findAll()).isEmpty();
    }

    @Test
    void rejectsAnUnknownCreator() {
        Question published = questionInStatus(QuestionStatus.PUBLISHED);

        assertThatThrownBy(() -> service.createQuiz(new CreateQuizCommand("Simulation", 999L,
                List.of(published.getId()))))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User");
        assertThat(quizzes.findAll()).isEmpty();
    }
}
