package com.example.questionbank.infrastructure.jpa;

import com.example.questionbank.domain.exception.ResourceNotFoundException;
import com.example.questionbank.domain.model.Competency;
import com.example.questionbank.domain.repository.CompetencyRepository;
import com.example.questionbank.infrastructure.jpa.mapper.CompetencyMapper;
import com.example.questionbank.infrastructure.jpa.record.CompetencyRecord;
import com.example.questionbank.infrastructure.jpa.repository.SpringCompetencyDataRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * JPA implementation of the {@link CompetencyRepository} contract.
 */
@Repository
public class CompetencyRepositoryJpa implements CompetencyRepository {

    private final SpringCompetencyDataRepository data;

    /**
     * Creates the adapter.
     *
     * @param data Spring Data competency repository
     */
    public CompetencyRepositoryJpa(SpringCompetencyDataRepository data) {
        this.data = data;
    }

    @Override
    @Transactional
    public Competency save(Competency competency) {
        if (competency.getId() == null) {
            return CompetencyMapper.toDomain(data.save(CompetencyMapper.toNewRecord(competency)));
        }
        CompetencyRecord record = data.findById(competency.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Competency not found: " + competency.getId()));
        record.update(competency.getName(), competency.getDescription());
        return CompetencyMapper.toDomain(data.save(record));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Competency> findById(Long id) {
        return data.findById(id).map(CompetencyMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Competency> findAll() {
        return data.findAll().stream().map(CompetencyMapper::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Competency> findAllById(Collection<Long> ids) {
        return data.findAllById(ids).stream().map(CompetencyMapper::toDomain).toList();
    }
}
