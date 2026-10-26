package com.example.questionbank.infrastructure.jpa.record;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * JPA record for quizzes and their many-to-many association with questions.
 */
@Entity
@Table(name = "quizzes")
public class QuizRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "creator_id", nullable = false)
    private UserRecord createdBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "quiz_questions",
            joinColumns = @JoinColumn(name = "quiz_id"),
            inverseJoinColumns = @JoinColumn(name = "question_id"))
    private Set<QuestionRecord> questions = new LinkedHashSet<>();

    /** Required by JPA. */
    protected QuizRecord() {
    }

    /**
     * Creates a quiz record.
     *
     * @param title quiz title
     * @param createdBy creator record
     * @param createdAt creation timestamp
     */
    public QuizRecord(String title, UserRecord createdBy, LocalDateTime createdAt) {
        this.title = title;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }

    /**
     * Updates the mutable columns of the record.
     *
     * @param title quiz title
     * @param createdBy creator record
     */
    public void update(String title, UserRecord createdBy) {
        this.title = title;
        this.createdBy = createdBy;
    }

    /** @return database identity */
    public Long getId() {
        return id;
    }

    /** @return quiz title */
    public String getTitle() {
        return title;
    }

    /** @return creator record */
    public UserRecord getCreatedBy() {
        return createdBy;
    }

    /** @return creation timestamp */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /** @return selected questions */
    public Set<QuestionRecord> getQuestions() {
        return questions;
    }
}
