package com.example.questionbank.domain.repository;

import com.example.questionbank.domain.model.Difficulty;
import com.example.questionbank.domain.model.MultipleChoiceQuestion;
import com.example.questionbank.domain.model.Question;
import com.example.questionbank.domain.model.QuestionStatus;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Contract that every {@link QuestionRepository} implementation must satisfy.
 *
 * <p>It is executed twice: against the in-memory fake and against the JPA
 * adapter, which proves that the domain contract can be tested without JPA.</p>
 */
public abstract class QuestionRepositoryContractTest {

    /**
     * Returns the implementation under test.
     *
     * @return question repository
     */
    protected abstract QuestionRepository questionRepository();

    /**
     * Persists an author and returns its identity.
     *
     * @param email unique email
     * @return author identity
     */
    protected abstract Long createAuthor(String email);

    /**
     * Persists a topic and returns its identity.
     *
     * @param name unique topic name
     * @return topic identity
     */
    protected abstract Long createTopic(String name);

    /**
     * Persists a competency and returns its identity.
     *
     * @param name unique competency name
     * @return competency identity
     */
    protected abstract Long createCompetency(String name);

    /**
     * Builds a valid draft multiple-choice question.
     *
     * @param authorId author identity
     * @param topicId topic identity
     * @param competencyId competency identity
     * @return draft question
     */
    protected MultipleChoiceQuestion newQuestion(Long authorId, Long topicId, Long competencyId) {
        MultipleChoiceQuestion question = new MultipleChoiceQuestion(null,
                "Which inheritance strategy stores common columns in a base table?",
                Difficulty.MEDIUM, QuestionStatus.DRAFT, authorId, topicId, Set.of(competencyId),
                null, null);
        for (int index = 1; index <= 5; index++) {
            question.addOption("Option " + index, index == 1);
        }
        return question;
    }

    /**
     * Moves a question through the review workflow and persists it as
     * published.
     *
     * @param question question to publish
     */
    protected void publish(Question question) {
        question.submitForReview();
        question.startReview();
        question.approve();
        question.publish();
        questionRepository().save(question);
    }

    @Test
    void savesAndFindsAQuestionById() {
        Long authorId = createAuthor("contract-author@questionbank.edu");
        Long topicId = createTopic("Contract Topic");
        Long competencyId = createCompetency("Contract Competency");

        Question saved = questionRepository().save(newQuestion(authorId, topicId, competencyId));

        assertThat(saved.getId()).isNotNull();
        Question found = questionRepository().findById(saved.getId()).orElseThrow();
        assertThat(found.getStatement()).contains("inheritance strategy");
        assertThat(found.getStatus()).isEqualTo(QuestionStatus.DRAFT);
        assertThat(found.getAuthorId()).isEqualTo(authorId);
        assertThat(found.getTopicId()).isEqualTo(topicId);
        assertThat(found.getCompetencyIds()).containsExactly(competencyId);
        assertThat(found).isInstanceOf(MultipleChoiceQuestion.class);
        MultipleChoiceQuestion multipleChoice = (MultipleChoiceQuestion) found;
        assertThat(multipleChoice.getOptions()).hasSize(5);
        assertThat(multipleChoice.getOptions().stream().filter(option -> option.isCorrect())).hasSize(1);
    }

    @Test
    void listsEveryQuestion() {
        Long authorId = createAuthor("contract-list@questionbank.edu");
        Long topicId = createTopic("List Topic");
        Long competencyId = createCompetency("List Competency");
        questionRepository().save(newQuestion(authorId, topicId, competencyId));
        questionRepository().save(newQuestion(authorId, topicId, competencyId));

        List<Question> all = questionRepository().findAll();

        assertThat(all).hasSize(2);
    }

    @Test
    void findsOnlyTheQuestionsOfAnAuthor() {
        Long firstAuthor = createAuthor("contract-first@questionbank.edu");
        Long secondAuthor = createAuthor("contract-second@questionbank.edu");
        Long topicId = createTopic("Author Topic");
        Long competencyId = createCompetency("Author Competency");
        Question first = questionRepository().save(newQuestion(firstAuthor, topicId, competencyId));
        questionRepository().save(newQuestion(secondAuthor, topicId, competencyId));

        List<Question> found = questionRepository().findByAuthorId(firstAuthor);

        assertThat(found).extracting(Question::getId).containsExactly(first.getId());
    }

    @Test
    void findsOnlyTheQuestionsInAGivenStatus() {
        Long authorId = createAuthor("contract-status@questionbank.edu");
        Long topicId = createTopic("Status Topic");
        Long competencyId = createCompetency("Status Competency");
        Question draft = questionRepository().save(newQuestion(authorId, topicId, competencyId));
        Question published = questionRepository().save(newQuestion(authorId, topicId, competencyId));
        publish(published);

        List<Question> drafts = questionRepository().findByStatus(QuestionStatus.DRAFT);
        List<Question> publishedQuestions = questionRepository().findByStatus(QuestionStatus.PUBLISHED);

        assertThat(drafts).extracting(Question::getId).contains(draft.getId());
        assertThat(drafts).extracting(Question::getId).doesNotContain(published.getId());
        assertThat(publishedQuestions).extracting(Question::getId).containsExactly(published.getId());
    }

    @Test
    void findsOnlyPublishedQuestionsForACompetency() {
        Long authorId = createAuthor("contract-competency@questionbank.edu");
        Long topicId = createTopic("Competency Topic");
        Long firstCompetency = createCompetency("First Competency");
        Long secondCompetency = createCompetency("Second Competency");
        Question published = questionRepository().save(newQuestion(authorId, topicId, firstCompetency));
        publish(published);
        Question draft = questionRepository().save(newQuestion(authorId, topicId, firstCompetency));
        Question otherCompetency = questionRepository().save(newQuestion(authorId, topicId, secondCompetency));
        publish(otherCompetency);

        List<Question> found = questionRepository().findPublishedByCompetencyId(firstCompetency);

        assertThat(found).extracting(Question::getId).containsExactly(published.getId());
        assertThat(found).extracting(Question::getId).doesNotContain(draft.getId(), otherCompetency.getId());
    }
}
