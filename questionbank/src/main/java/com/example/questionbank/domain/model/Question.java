package com.example.questionbank.domain.model;

import com.example.questionbank.domain.exception.DomainException;
import com.example.questionbank.domain.exception.InvalidStateTransitionException;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Root of the question hierarchy and owner of its lifecycle rules.
 *
 * <p>This class contains no JPA annotations or imports: persistence is a
 * separate model translated by the infrastructure mappers. State changes are
 * exposed as intention-revealing methods so that no caller can assign an
 * arbitrary status.</p>
 */
public abstract class Question {

    private Long id;
    private final String statement;
    private final Difficulty difficulty;
    private QuestionStatus status;
    private final Long authorId;
    private Long topicId;
    private final Set<Long> competencyIds;
    private final QuestionMetadata metadata;

    /**
     * Creates or rehydrates a question.
     *
     * <p>The constructor validates the structural invariants of the model. It
     * deliberately does not enforce the lifecycle: rehydrating a question from
     * the database must not re-run the review rules.</p>
     *
     * @param id identity assigned by the repository, or {@code null}
     * @param statement question wording
     * @param difficulty cognitive difficulty
     * @param status current lifecycle state
     * @param authorId identity of the author
     * @param topicId identity of the topic
     * @param competencyIds identities of the evaluated competencies
     * @param metadata optional editorial metadata
     */
    protected Question(Long id, String statement, Difficulty difficulty, QuestionStatus status,
                       Long authorId, Long topicId, Collection<Long> competencyIds,
                       QuestionMetadata metadata) {
        if (statement == null || statement.isBlank()) {
            throw new DomainException("The question statement is required");
        }
        if (difficulty == null) {
            throw new DomainException("The question difficulty is required");
        }
        if (status == null) {
            throw new DomainException("The question status is required");
        }
        if (authorId == null) {
            throw new DomainException("The question author is required");
        }
        if (topicId == null) {
            throw new DomainException("The question topic is required");
        }
        this.id = id;
        this.statement = statement.trim();
        this.difficulty = difficulty;
        this.status = status;
        this.authorId = authorId;
        this.topicId = topicId;
        this.competencyIds = new LinkedHashSet<>(competencyIds == null ? Set.of() : competencyIds);
        this.metadata = metadata;
    }

    /**
     * Validates the minimum content and moves the question to
     * {@link QuestionStatus#PENDING_REVIEW}.
     *
     * @throws DomainException when the question is not ready for review
     * @throws InvalidStateTransitionException when the current state cannot be submitted
     */
    public void submitForReview() {
        validateForReview();
        transitionTo(QuestionStatus.PENDING_REVIEW);
    }

    /**
     * Moves a pending question to {@link QuestionStatus#IN_REVIEW}.
     *
     * @throws InvalidStateTransitionException when the transition is illegal
     */
    public void startReview() {
        transitionTo(QuestionStatus.IN_REVIEW);
    }

    /**
     * Approves a question that is being reviewed.
     *
     * @throws InvalidStateTransitionException when the transition is illegal
     */
    public void approve() {
        transitionTo(QuestionStatus.APPROVED);
    }

    /**
     * Rejects the question and returns it to {@link QuestionStatus#DRAFT}.
     *
     * @throws InvalidStateTransitionException when the transition is illegal
     */
    public void reject() {
        transitionTo(QuestionStatus.DRAFT);
    }

    /**
     * Publishes an approved question so it can be used in quizzes.
     *
     * @throws InvalidStateTransitionException when the transition is illegal
     */
    public void publish() {
        transitionTo(QuestionStatus.PUBLISHED);
    }

    /**
     * Archives the question, removing it from active use.
     *
     * @throws InvalidStateTransitionException when the transition is illegal
     */
    public void archive() {
        transitionTo(QuestionStatus.ARCHIVED);
    }

    /**
     * Checks the minimum conditions to submit the question for review.
     *
     * @throws DomainException when a required condition is missing
     */
    public void validateForReview() {
        if (competencyIds.isEmpty()) {
            throw new DomainException("At least one competency is required before review");
        }
        validateTypeSpecificRules();
    }

    /**
     * Validates the rules that depend on the concrete question type.
     *
     * @throws DomainException when the type-specific content is incomplete
     */
    protected abstract void validateTypeSpecificRules();

    /**
     * Replaces the topic and competencies associated with the question.
     *
     * @param topicId identity of the new topic
     * @param competencyIds identities of the new competencies
     * @throws DomainException when the classification is incomplete
     */
    public void classify(Long topicId, Collection<Long> competencyIds) {
        if (topicId == null) {
            throw new DomainException("The question topic is required");
        }
        if (competencyIds == null || competencyIds.isEmpty()) {
            throw new DomainException("At least one competency is required");
        }
        this.topicId = topicId;
        this.competencyIds.clear();
        this.competencyIds.addAll(competencyIds);
    }

    /**
     * Assigns the identity generated by the persistence layer.
     *
     * <p>Only repository implementations should call this method; it never
     * changes the lifecycle state.</p>
     *
     * @param id generated identity
     */
    public void assignIdentity(Long id) {
        this.id = id;
    }

    private void transitionTo(QuestionStatus target) {
        if (!status.canTransitionTo(target)) {
            throw new InvalidStateTransitionException(
                    "Cannot move question " + id + " from " + status + " to " + target);
        }
        this.status = target;
    }

    /** @return {@code true} when the question can be used in a quiz */
    public boolean isPublished() {
        return status == QuestionStatus.PUBLISHED;
    }

    /** @return identity assigned by the repository, or {@code null} */
    public Long getId() {
        return id;
    }

    /** @return question wording */
    public String getStatement() {
        return statement;
    }

    /** @return cognitive difficulty */
    public Difficulty getDifficulty() {
        return difficulty;
    }

    /** @return current lifecycle state */
    public QuestionStatus getStatus() {
        return status;
    }

    /** @return identity of the author */
    public Long getAuthorId() {
        return authorId;
    }

    /** @return identity of the topic */
    public Long getTopicId() {
        return topicId;
    }

    /** @return identities of the evaluated competencies */
    public Set<Long> getCompetencyIds() {
        return Set.copyOf(competencyIds);
    }

    /** @return editorial metadata, or {@code null} */
    public QuestionMetadata getMetadata() {
        return metadata;
    }
}
