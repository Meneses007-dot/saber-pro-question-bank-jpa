package com.example.questionbank.application;

import com.example.questionbank.domain.exception.DomainException;
import com.example.questionbank.domain.exception.ResourceNotFoundException;
import com.example.questionbank.domain.model.Competency;
import com.example.questionbank.domain.model.Difficulty;
import com.example.questionbank.domain.model.MultipleChoiceQuestion;
import com.example.questionbank.domain.model.Question;
import com.example.questionbank.domain.model.QuestionStatus;
import com.example.questionbank.domain.model.QuestionType;
import com.example.questionbank.domain.model.Role;
import com.example.questionbank.domain.model.Topic;
import com.example.questionbank.domain.model.TrueFalseQuestion;
import com.example.questionbank.domain.model.User;
import com.example.questionbank.support.InMemoryCompetencyRepository;
import com.example.questionbank.support.InMemoryQuestionRepository;
import com.example.questionbank.support.InMemoryTopicRepository;
import com.example.questionbank.support.InMemoryUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests of the question use cases using in-memory repositories.
 */
class QuestionServiceTest {

    private InMemoryQuestionRepository questions;
    private InMemoryUserRepository users;
    private InMemoryTopicRepository topics;
    private InMemoryCompetencyRepository competencies;
    private QuestionService service;

    private Long authorId;
    private Long topicId;
    private Long competencyId;

    @BeforeEach
    void setUp() {
        questions = new InMemoryQuestionRepository();
        users = new InMemoryUserRepository();
        topics = new InMemoryTopicRepository();
        competencies = new InMemoryCompetencyRepository();
        service = new QuestionService(questions, users, topics, competencies);
        authorId = users.save(new User(null, "Arturo Author", "author@test.edu", Role.AUTHOR)).getId();
        topicId = topics.save(new Topic(null, "Software Architecture")).getId();
        competencyId = competencies.save(new Competency(null, "Solution Design", "Design")).getId();
    }

    private CreateQuestionCommand multipleChoiceCommand(Set<Long> competencyIds) {
        List<CreateQuestionCommand.OptionData> options = List.of(
                new CreateQuestionCommand.OptionData("Option 1", true),
                new CreateQuestionCommand.OptionData("Option 2", false),
                new CreateQuestionCommand.OptionData("Option 3", false),
                new CreateQuestionCommand.OptionData("Option 4", false),
                new CreateQuestionCommand.OptionData("Option 5", false));
        return new CreateQuestionCommand("Which strategy uses a base table?", authorId, topicId,
                competencyIds, QuestionType.MULTIPLE_CHOICE, Difficulty.MEDIUM, options, null, null);
    }

    @Test
    void createsAMultipleChoiceQuestion() {
        Question question = service.createQuestion(multipleChoiceCommand(Set.of(competencyId)));

        assertThat(question.getId()).isNotNull();
        assertThat(question.getStatus()).isEqualTo(QuestionStatus.DRAFT);
        assertThat(question).isInstanceOf(MultipleChoiceQuestion.class);
        assertThat(((MultipleChoiceQuestion) question).getOptions()).hasSize(5);
        assertThat(questions.findAll()).hasSize(1);
    }

    @Test
    void createsATrueFalseQuestion() {
        CreateQuestionCommand command = new CreateQuestionCommand("JOINED uses a base table", authorId,
                topicId, Set.of(competencyId), QuestionType.TRUE_FALSE, Difficulty.EASY,
                null, Boolean.TRUE, null);

        Question question = service.createQuestion(command);

        assertThat(question).isInstanceOf(TrueFalseQuestion.class);
        assertThat(((TrueFalseQuestion) question).getExpectedAnswer()).isTrue();
    }

    @Test
    void rejectsAnUnknownAuthor() {
        CreateQuestionCommand command = new CreateQuestionCommand("Statement", 999L, topicId,
                Set.of(competencyId), QuestionType.TRUE_FALSE, Difficulty.EASY, null, true, null);

        assertThatThrownBy(() -> service.createQuestion(command))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Author");
        assertThat(questions.findAll()).isEmpty();
    }

    @Test
    void rejectsAnUnknownCompetency() {
        CreateQuestionCommand command = multipleChoiceCommand(Set.of(999L));

        assertThatThrownBy(() -> service.createQuestion(command))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Competencies");
        assertThat(questions.findAll()).isEmpty();
    }

    @Test
    void classifiesAnExistingQuestion() {
        Question question = service.createQuestion(multipleChoiceCommand(Set.of()));
        Long otherTopic = topics.save(new Topic(null, "Databases")).getId();
        Long otherCompetency = competencies.save(new Competency(null, "Persistence", "Data")).getId();

        Question classified = service.classifyQuestion(question.getId(),
                new ClassifyQuestionCommand(otherTopic, Set.of(otherCompetency)));

        assertThat(classified.getTopicId()).isEqualTo(otherTopic);
        assertThat(classified.getCompetencyIds()).containsExactly(otherCompetency);
    }

    @Test
    void submittingAnIncompleteQuestionFails() {
        Question question = service.createQuestion(multipleChoiceCommand(Set.of()));

        assertThatThrownBy(() -> service.submitForReview(question.getId()))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("competency");
        assertThat(service.getQuestion(question.getId()).getStatus()).isEqualTo(QuestionStatus.DRAFT);
    }

    @Test
    void submitsACompleteQuestion() {
        Question question = service.createQuestion(multipleChoiceCommand(Set.of(competencyId)));

        Question submitted = service.submitForReview(question.getId());

        assertThat(submitted.getStatus()).isEqualTo(QuestionStatus.PENDING_REVIEW);
    }

    @Test
    void filtersQuestionsByAuthorAndStatus() {
        Question first = service.createQuestion(multipleChoiceCommand(Set.of(competencyId)));
        Long otherAuthor = users.save(new User(null, "Other", "other@test.edu", Role.AUTHOR)).getId();
        service.createQuestion(new CreateQuestionCommand("Other statement", otherAuthor, topicId,
                Set.of(competencyId), QuestionType.TRUE_FALSE, Difficulty.EASY, null, true, null));
        service.submitForReview(first.getId());

        List<Question> result = service.listQuestions(authorId, QuestionStatus.PENDING_REVIEW);

        assertThat(result).extracting(Question::getId).containsExactly(first.getId());
    }
}
