package com.example.questionbank.infrastructure.jpa.record;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * JPA representation of a question topic.
 */
@Entity
@Table(name = "topics")
public class TopicRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 150)
    private String name;

    /** Required by JPA. */
    protected TopicRecord() {
    }

    /**
     * Creates a topic record.
     *
     * @param name unique name
     */
    public TopicRecord(String name) {
        this.name = name;
    }

    /**
     * Updates the mutable columns of the record.
     *
     * @param name unique name
     */
    public void update(String name) {
        this.name = name;
    }

    /** @return database identity */
    public Long getId() {
        return id;
    }

    /** @return topic name */
    public String getName() {
        return name;
    }
}
