package com.example.questionbank.domain.repository;

import com.example.questionbank.domain.model.Competency;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Persistence contract for academic competencies.
 */
public interface CompetencyRepository {

    /**
     * Inserts or updates a competency.
     *
     * @param competency competency to persist
     * @return the persisted competency
     */
    Competency save(Competency competency);

    /**
     * Finds a competency by identity.
     *
     * @param id competency identity
     * @return the competency when it exists
     */
    Optional<Competency> findById(Long id);

    /**
     * Lists every competency.
     *
     * @return all competencies
     */
    List<Competency> findAll();

    /**
     * Finds several competencies at once.
     *
     * @param ids competencies to look up
     * @return the competencies that exist
     */
    List<Competency> findAllById(Collection<Long> ids);
}
