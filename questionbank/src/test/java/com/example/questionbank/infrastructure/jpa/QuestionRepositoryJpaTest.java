package com.example.questionbank.infrastructure.jpa;

import com.example.questionbank.domain.exception.ResourceNotFoundException;
import com.example.questionbank.domain.model.AnswerOption;
import com.example.questionbank.domain.model.Competency;
import com.example.questionbank.domain.model.Difficulty;
import com.example.questionbank.domain.model.MultipleChoiceQuestion;
import com.example.questionbank.domain.model.Question;
import com.example.questionbank.domain.model.QuestionMetadata;
import com.example.questionbank.domain.model.QuestionStatus;
import com.example.questionbank.domain.model.Role;
import com.example.questionbank.domain.model.Topic;
import com.example.questionbank.domain.model.TrueFalseQuestion;
import com.example.questionbank.domain.model.User;
import com.example.questionbank.domain.repository.CompetencyRepository;
import com.example.questionbank.domain.repository.QuestionRepository;
import com.example.questionbank.domain.repository.TopicRepository;
import com.example.questionbank.domain.repository.UserRepository;
import com.example.questionbank.infrastructure.jpa.record.QuestionMetadataRecord;
import com.example.questionbank.infrastructure.jpa.record.QuestionRecord;
import com.example.questionbank.support.DatabaseCleaner;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests of the JPA question adapter against H2.
 *
 * <p>The tests flush and clear the persistence context so every assertion
 * reads from the database instead of the first-level cache.</p>
 */
@DataJpaTest
@Import({QuestionRepositoryJpa.class, UserRepositoryJpa.class,
        TopicRepositoryJpa.class, CompetencyRepositoryJpa.class})
class QuestionRepositoryJpaTest {

    @Autowired
    private QuestionRepository questions;

    @Autowired
    private UserRepository users;

    @Autowired
    private TopicRepository topics;

    @Autowired
    private CompetencyRepository competencies;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PersistenceContext
    private EntityManager entityManager;

    private Long authorId;
    private Long topicId;
    private Long firstCompetencyId;
    private Long secondCompetencyId;

    @BeforeEach
    void setUp() {
        DatabaseCleaner.clean(jdbcTemplate);
        authorId = users.save(new User(null, "Arturo Author", "author@jpa.edu", Role.AUTHOR)).getId();
        topicId = topics.save(new Topic(null, "Software Architecture")).getId();
        firstCompetencyId = competencies.save(new Competency(null, "Solution Design", "Design")).getId();
        secondCompetencyId = competencies.save(new Competency(null, "Problem Analysis", "Analysis")).getId();
    }

    private MultipleChoiceQuestion multipleChoice(QuestionMetadata metadata) {
        MultipleChoiceQuestion question = new MultipleChoiceQuestion(null,
                "Which strategy stores common columns in a base table?", Difficulty.MEDIUM,
                QuestionStatus.DRAFT, authorId, topicId, Set.of(firstCompetencyId, secondCompetencyId),
                metadata, null);
        for (int index = 1; index <= 5; index++) {
            question.addOption("Option " + index, index == 1);
        }
        return question;
    }

    @Test
    void savesAQuestionWithAllItsAssociationsAndReadsThemBack() {
        QuestionMetadata metadata = new QuestionMetadata(LocalDate.of(2026, 3, 1),
                "ANALYZE", 3, "Editorial note");

        Question saved = questions.save(multipleChoice(metadata));
        entityManager.flush();
        entityManager.clear();

        Question found = questions.findById(saved.getId()).orElseThrow();
        assertThat(found.getId()).isNotNull();
        assertThat(found.getStatus()).isEqualTo(QuestionStatus.DRAFT);
        assertThat(found.getAuthorId()).isEqualTo(authorId);
        assertThat(found.getTopicId()).isEqualTo(topicId);
        assertThat(found.getCompetencyIds())
                .containsExactlyInAnyOrder(firstCompetencyId, secondCompetencyId);
        assertThat(found.getMetadata()).isNotNull();
        assertThat(found.getMetadata().getCognitiveLevel()).isEqualTo("ANALYZE");
        assertThat(found.getMetadata().getEstimatedMinutes()).isEqualTo(3);
        assertThat(found).isInstanceOf(MultipleChoiceQuestion.class);
        List<AnswerOption> options = ((MultipleChoiceQuestion) found).getOptions();
        assertThat(options).hasSize(5);
        assertThat(options.stream().filter(AnswerOption::isCorrect)).hasSize(1);
        assertThat(options).extracting(AnswerOption::getId).doesNotContainNull();
    }

    @Test
    void updatesAnExistingQuestionInsteadOfInsertingADuplicate() {
        Question saved = questions.save(multipleChoice(null));
        saved.submitForReview();

        questions.save(saved);
        entityManager.flush();
        entityManager.clear();

        Number count = (Number) entityManager
                .createNativeQuery("select count(*) from questions").getSingleResult();
        assertThat(count.intValue()).isEqualTo(1);
        assertThat(questions.findById(saved.getId()).orElseThrow().getStatus())
                .isEqualTo(QuestionStatus.PENDING_REVIEW);
    }

    @Test
    void persistsBothQuestionTypesWithJoinedInheritance() {
        MultipleChoiceQuestion multipleChoice = multipleChoice(null);
        TrueFalseQuestion trueFalse = new TrueFalseQuestion(null, "JOINED uses a base table",
                Difficulty.EASY, QuestionStatus.DRAFT, authorId, topicId, Set.of(firstCompetencyId),
                null, Boolean.TRUE);
        questions.save(multipleChoice);
        questions.save(trueFalse);
        entityManager.flush();
        entityManager.clear();

        List<Question> all = questions.findAll();
        assertThat(all).hasSize(2);
        assertThat(all).anySatisfy(question -> assertThat(question)
                .isInstanceOf(MultipleChoiceQuestion.class));
        assertThat(all).anySatisfy(question -> {
            assertThat(question).isInstanceOf(TrueFalseQuestion.class);
            assertThat(((TrueFalseQuestion) question).getExpectedAnswer()).isTrue();
        });

        Number tables = (Number) entityManager.createNativeQuery(
                        "select count(*) from information_schema.tables "
                                + "where table_name in ('QUESTIONS','MULTIPLE_CHOICE_QUESTIONS',"
                                + "'TRUE_FALSE_QUESTIONS')")
                .getSingleResult();
        assertThat(tables.intValue()).isEqualTo(3);
    }

    @Test
    void rejectsAQuestionWithAnUnknownAuthor() {
        MultipleChoiceQuestion question = new MultipleChoiceQuestion(null, "Statement",
                Difficulty.EASY, QuestionStatus.DRAFT, 999L, topicId, Set.of(firstCompetencyId),
                null, List.of());

        assertThatThrownBy(() -> questions.save(question))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Author");
    }

    @Test
    void rejectsAQuestionWithAnUnknownTopic() {
        MultipleChoiceQuestion question = new MultipleChoiceQuestion(null, "Statement",
                Difficulty.EASY, QuestionStatus.DRAFT, authorId, 999L, Set.of(firstCompetencyId),
                null, List.of());

        assertThatThrownBy(() -> questions.save(question))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Topic");
    }

    @Test
    void theDatabaseRejectsTwoMetadataRowsForTheSameQuestion() {
        Question saved = questions.save(multipleChoice(
                new QuestionMetadata(LocalDate.of(2026, 3, 1), "ANALYZE", 3, "Note")));
        entityManager.flush();
        QuestionRecord record = entityManager.find(QuestionRecord.class, saved.getId());
        QuestionMetadataRecord duplicate = new QuestionMetadataRecord(record,
                LocalDate.of(2026, 4, 1), "APPLY", 2, "Duplicate");

        assertThatThrownBy(() -> {
            entityManager.persist(duplicate);
            entityManager.flush();
        }).isInstanceOf(PersistenceException.class);
    }

    @Test
    void deletingAQuestionDeletesItsOptionsAndMetadata() {
        Question saved = questions.save(multipleChoice(
                new QuestionMetadata(LocalDate.of(2026, 3, 1), "ANALYZE", 3, "Note")));
        entityManager.flush();
        entityManager.clear();
        Long questionId = saved.getId();

        QuestionRecord record = entityManager.find(QuestionRecord.class, questionId);
        entityManager.remove(record);
        entityManager.flush();
        entityManager.clear();

        Number options = (Number) entityManager.createNativeQuery(
                "select count(*) from answer_options where question_id = " + questionId).getSingleResult();
        Number metadata = (Number) entityManager.createNativeQuery(
                "select count(*) from question_metadata where question_id = " + questionId).getSingleResult();
        assertThat(options.intValue()).isZero();
        assertThat(metadata.intValue()).isZero();
    }
}
