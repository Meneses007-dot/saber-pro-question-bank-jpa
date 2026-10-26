package com.example.questionbank.infrastructure.jpa.mapper;

import com.example.questionbank.domain.model.Competency;
import com.example.questionbank.infrastructure.jpa.record.CompetencyRecord;

/**
 * Translates competencies between the domain and the JPA model.
 */
public final class CompetencyMapper {

    private CompetencyMapper() {
    }

    /**
     * Converts a JPA record to a domain competency.
     *
     * @param record JPA record
     * @return domain competency
     */
    public static Competency toDomain(CompetencyRecord record) {
        return new Competency(record.getId(), record.getName(), record.getDescription());
    }

    /**
     * Creates a new JPA record from a domain competency.
     *
     * @param competency domain competency
     * @return new JPA record
     */
    public static CompetencyRecord toNewRecord(Competency competency) {
        return new CompetencyRecord(competency.getName(), competency.getDescription());
    }
}
