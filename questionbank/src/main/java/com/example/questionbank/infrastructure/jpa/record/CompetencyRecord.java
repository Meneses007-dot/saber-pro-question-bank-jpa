package com.example.questionbank.infrastructure.jpa.record;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * JPA representation of an academic competency.
 */
@Entity
@Table(name = "competencies")
public class CompetencyRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 150)
    private String name;

    @Column(length = 500)
    private String description;

    /** Required by JPA. */
    protected CompetencyRecord() {
    }

    /**
     * Creates a competency record.
     *
     * @param name unique name
     * @param description optional description
     */
    public CompetencyRecord(String name, String description) {
        this.name = name;
        this.description = description;
    }

    /**
     * Updates the mutable columns of the record.
     *
     * @param name unique name
     * @param description optional description
     */
    public void update(String name, String description) {
        this.name = name;
        this.description = description;
    }

    /** @return database identity */
    public Long getId() {
        return id;
    }

    /** @return competency name */
    public String getName() {
        return name;
    }

    /** @return optional description */
    public String getDescription() {
        return description;
    }
}
