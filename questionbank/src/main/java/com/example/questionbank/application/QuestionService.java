package com.example.questionbank.application;

import com.example.questionbank.domain.exception.DomainException;
import com.example.questionbank.domain.exception.ResourceNotFoundException;
import com.example.questionbank.domain.model.Competency;
import com.example.questionbank.domain.model.MultipleChoiceQuestion;
import com.example.questionbank.domain.model.Question;
import com.example.questionbank.domain.model.QuestionStatus;
import com.example.questionbank.domain.model.QuestionType;
import com.example.questionbank.domain.model.Topic;
import com.example.questionbank.domain.model.TrueFalseQuestion;
import com.example.questionbank.domain.model.User;
import com.example.questionbank.domain.repository.CompetencyRepository;
import com.example.questionbank.domain.repository.QuestionRepository;
import com.example.questionbank.domain.repository.TopicRepository;
import com.example.questionbank.domain.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Application use cases for creating, querying, classifying and submitting
 * questions.
 *
 * <p>This service depends only on domain contracts; it never references Spring
 * Data or JPA types.</p>
 */
@Service
@Transactional
public class QuestionService {

    private final QuestionRepository questions;
    private final UserRepository users;
    private final TopicRepository topics;
    private final CompetencyRepository competencies;

    /**
     * Injects the domain repository contracts.
     *
     * @param questions question repository
     * @param users user repository
     * @param topics topic repository
     * @param competencies competency repository
     */
    public QuestionService(QuestionRepository questions, UserRepository users,
                           TopicRepository topics, CompetencyRepository competencies) {
        this.questions = questions;
        this.users = users;
        this.topics = topics;
        this.competencies = competencies;
    }

    /**
     * Creates a draft question of the requested type.
     *
     * @param command creation data
     * @return the persisted question
     * @throws ResourceNotFoundException when the author, topic or a competency does not exist
     * @throws DomainException when the supplied data violates a domain rule
     */
    public Question createQuestion(CreateQuestionCommand command) {
        if (command.type() == null) {
            throw new DomainException("The question type is required");
        }
        if (command.authorId() == null) {
            throw new DomainException("The question author is required");
        }
        if (command.topicId() == null) {
            throw new DomainException("The question topic is required");
        }
        User author = users.findById(command.authorId())
                .orElseThrow(() -> new ResourceNotFoundException("Author not found: " + command.authorId()));
        Topic topic = topics.findById(command.topicId())
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found: " + command.topicId()));
        Set<Long> competencyIds = command.competencyIds() == null
                ? Set.of() : new LinkedHashSet<>(command.competencyIds());
        validateCompetencies(competencyIds);

        Question question = buildQuestion(command, author.getId(), topic.getId(), competencyIds);
        return questions.save(question);
    }

    private Question buildQuestion(CreateQuestionCommand command, Long authorId, Long topicId,
                                   Set<Long> competencyIds) {
        if (command.type() == QuestionType.MULTIPLE_CHOICE) {
            MultipleChoiceQuestion question = new MultipleChoiceQuestion(null, command.statement(),
                    command.difficulty(), QuestionStatus.DRAFT, authorId, topicId, competencyIds,
                    command.metadata(), null);
            if (command.options() != null) {
                command.options().forEach(option -> question.addOption(option.text(), option.correct()));
            }
            return question;
        }
        return new TrueFalseQuestion(null, command.statement(), command.difficulty(),
                QuestionStatus.DRAFT, authorId, topicId, competencyIds, command.metadata(),
                command.expectedAnswer());
    }

    /**
     * Finds a question or fails.
     *
     * @param id question identity
     * @return the question
     * @throws ResourceNotFoundException when the question does not exist
     */
    @Transactional(readOnly = true)
    public Question getQuestion(Long id) {
        return findQuestion(id);
    }

    /**
     * Lists questions filtered by author, lifecycle state or both.
     *
     * @param authorId optional author identity
     * @param status optional lifecycle state
     * @return matching questions
     */
    @Transactional(readOnly = true)
    public List<Question> listQuestions(Long authorId, QuestionStatus status) {
        if (authorId != null && status != null) {
            return questions.findByAuthorId(authorId).stream()
                    .filter(question -> question.getStatus() == status)
                    .toList();
        }
        if (authorId != null) {
            return questions.findByAuthorId(authorId);
        }
        if (status != null) {
            return questions.findByStatus(status);
        }
        return questions.findAll();
    }

    /**
     * Lists the published questions that evaluate a competency.
     *
     * @param competencyId competency identity
     * @return published questions associated with the competency
     * @throws ResourceNotFoundException when the competency does not exist
     */
    @Transactional(readOnly = true)
    public List<Question> findPublishedByCompetency(Long competencyId) {
        competencies.findById(competencyId)
                .orElseThrow(() -> new ResourceNotFoundException("Competency not found: " + competencyId));
        return questions.findPublishedByCompetencyId(competencyId);
    }

    /**
     * Replaces the topic and competencies of a question.
     *
     * @param questionId question identity
     * @param command new classification
     * @return the updated question
     * @throws ResourceNotFoundException when the question, topic or a competency does not exist
     */
    public Question classifyQuestion(Long questionId, ClassifyQuestionCommand command) {
        Question question = findQuestion(questionId);
        if (command.topicId() == null) {
            throw new DomainException("The question topic is required");
        }
        Topic topic = topics.findById(command.topicId())
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found: " + command.topicId()));
        Set<Long> competencyIds = command.competencyIds() == null
                ? Set.of() : new LinkedHashSet<>(command.competencyIds());
        validateCompetencies(competencyIds);
        question.classify(topic.getId(), competencyIds);
        return questions.save(question);
    }

    /**
     * Sends a question to review after validating its minimum content.
     *
     * @param questionId question identity
     * @return the updated question
     * @throws DomainException when the question is not ready for review
     */
    public Question submitForReview(Long questionId) {
        Question question = findQuestion(questionId);
        question.submitForReview();
        return questions.save(question);
    }

    private Question findQuestion(Long id) {
        return questions.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found: " + id));
    }

    private void validateCompetencies(Collection<Long> competencyIds) {
        if (competencyIds.isEmpty()) {
            return;
        }
        List<Competency> found = competencies.findAllById(competencyIds);
        if (found.size() != competencyIds.size()) {
            List<Long> missing = new ArrayList<>(competencyIds);
            found.forEach(competency -> missing.remove(competency.getId()));
            throw new ResourceNotFoundException("Competencies not found: " + missing);
        }
    }
}
