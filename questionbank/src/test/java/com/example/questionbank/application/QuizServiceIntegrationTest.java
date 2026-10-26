package com.example.questionbank.application;

import com.example.questionbank.domain.exception.DomainException;
import com.example.questionbank.domain.model.Competency;
import com.example.questionbank.domain.model.Difficulty;
import com.example.questionbank.domain.model.Question;
import com.example.questionbank.domain.model.QuestionStatus;
import com.example.questionbank.domain.model.QuestionType;
import com.example.questionbank.domain.model.Quiz;
import com.example.questionbank.domain.model.Role;
import com.example.questionbank.domain.model.Topic;
import com.example.questionbank.domain.model.User;
import com.example.questionbank.domain.repository.CompetencyRepository;
import com.example.questionbank.domain.repository.QuestionRepository;
import com.example.questionbank.domain.repository.QuizRepository;
import com.example.questionbank.domain.repository.TopicRepository;
import com.example.questionbank.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * End-to-end test of the quiz use case over Spring Boot and H2.
 */
@SpringBootTest
class QuizServiceIntegrationTest {

    @Autowired
    private QuestionService questionService;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private QuizService quizService;

    @Autowired
    private UserRepository users;

    @Autowired
    private TopicRepository topics;

    @Autowired
    private CompetencyRepository competencies;

    @Autowired
    private QuestionRepository questions;

    @Autowired
    private QuizRepository quizzes;

    private Long authorId;
    private Long teacherId;
    private Long topicId;
    private Long competencyId;

    @BeforeEach
    void setUp() {
        String suffix = UUID.randomUUID().toString();
        authorId = users.save(new User(null, "Author " + suffix, "author-" + suffix + "@test.edu",
                Role.AUTHOR)).getId();
        teacherId = users.save(new User(null, "Teacher " + suffix, "teacher-" + suffix + "@test.edu",
                Role.TEACHER)).getId();
        topicId = topics.save(new Topic(null, "Topic " + suffix)).getId();
        competencyId = competencies.save(new Competency(null, "Competency " + suffix, "Description")).getId();
    }

    private Question createQuestion(String statement) {
        List<CreateQuestionCommand.OptionData> options = List.of(
                new CreateQuestionCommand.OptionData("Option 1", true),
                new CreateQuestionCommand.OptionData("Option 2", false),
                new CreateQuestionCommand.OptionData("Option 3", false),
                new CreateQuestionCommand.OptionData("Option 4", false),
                new CreateQuestionCommand.OptionData("Option 5", false));
        return questionService.createQuestion(new CreateQuestionCommand(statement, authorId, topicId,
                Set.of(competencyId), QuestionType.MULTIPLE_CHOICE, Difficulty.MEDIUM, options, null, null));
    }

    private Question publish(Question question) {
        questionService.submitForReview(question.getId());
        reviewService.startReview(question.getId());
        reviewService.approve(question.getId());
        return reviewService.publish(question.getId());
    }

    @Test
    void createsAQuizWithPublishedQuestionsEndToEnd() {
        Question question = createQuestion("End-to-end published question " + UUID.randomUUID());
        publish(question);
        String title = "Simulation " + UUID.randomUUID();

        Quiz quiz = quizService.createQuiz(new CreateQuizCommand(title, teacherId,
                List.of(question.getId())));

        assertThat(quiz.getId()).isNotNull();
        assertThat(quiz.getQuestionIds()).containsExactly(question.getId());
        assertThat(quizzes.findById(quiz.getId())).isPresent();
        assertThat(questions.findById(question.getId()).orElseThrow().getStatus())
                .isEqualTo(QuestionStatus.PUBLISHED);
    }

    @Test
    void rejectsAQuizWithANonPublishedQuestionEndToEnd() {
        Question draft = createQuestion("End-to-end draft question " + UUID.randomUUID());
        String title = "Rejected simulation " + UUID.randomUUID();

        assertThatThrownBy(() -> quizService.createQuiz(new CreateQuizCommand(title, teacherId,
                List.of(draft.getId()))))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("PUBLISHED");
        assertThat(quizzes.findAll()).noneMatch(quiz -> quiz.getTitle().equals(title));
    }
}
