package com.example.questionbank.infrastructure.jpa.record;

import com.example.questionbank.domain.model.Difficulty;
import com.example.questionbank.domain.model.QuestionStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Base JPA record of the question hierarchy.
 *
 * <p>The hierarchy uses the {@code JOINED} strategy: common columns live in
 * {@code questions} and type-specific columns in the subclass tables.</p>
 */
@Entity
@Table(name = "questions")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "question_type", discriminatorType = DiscriminatorType.STRING, length = 30)
public abstract class QuestionRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 2000)
    private String statement;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Difficulty difficulty;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private QuestionStatus status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private UserRecord author;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "topic_id", nullable = false)
    private TopicRecord topic;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "question_competencies",
            joinColumns = @JoinColumn(name = "question_id"),
            inverseJoinColumns = @JoinColumn(name = "competency_id"))
    private Set<CompetencyRecord> competencies = new LinkedHashSet<>();

    @OneToOne(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private QuestionMetadataRecord metadata;

    /** Required by JPA. */
    protected QuestionRecord() {
    }

    /**
     * Creates the common part of a question record.
     *
     * @param statement question wording
     * @param difficulty cognitive difficulty
     * @param status lifecycle state
     * @param author author record
     * @param topic topic record
     */
    protected QuestionRecord(String statement, Difficulty difficulty, QuestionStatus status,
                             UserRecord author, TopicRecord topic) {
        this.statement = statement;
        this.difficulty = difficulty;
        this.status = status;
        this.author = author;
        this.topic = topic;
    }

    /**
     * Updates the common columns of the record.
     *
     * @param statement question wording
     * @param difficulty cognitive difficulty
     * @param status lifecycle state
     * @param author author record
     * @param topic topic record
     */
    public void update(String statement, Difficulty difficulty, QuestionStatus status,
                       UserRecord author, TopicRecord topic) {
        this.statement = statement;
        this.difficulty = difficulty;
        this.status = status;
        this.author = author;
        this.topic = topic;
    }

    /** @return database identity */
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

    /** @return lifecycle state */
    public QuestionStatus getStatus() {
        return status;
    }

    /** @return author record */
    public UserRecord getAuthor() {
        return author;
    }

    /** @return topic record */
    public TopicRecord getTopic() {
        return topic;
    }

    /** @return evaluated competencies */
    public Set<CompetencyRecord> getCompetencies() {
        return competencies;
    }

    /** @return editorial metadata, or {@code null} */
    public QuestionMetadataRecord getMetadata() {
        return metadata;
    }

    /**
     * Replaces the metadata, maintaining both sides of the one-to-one
     * association.
     *
     * @param metadata metadata record, or {@code null}
     */
    public void setMetadata(QuestionMetadataRecord metadata) {
        this.metadata = metadata;
        if (metadata != null && metadata.getQuestion() != this) {
            metadata.setQuestion(this);
        }
    }
}
