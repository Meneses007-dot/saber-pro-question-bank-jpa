package com.example.questionbank.support;

import com.example.questionbank.domain.model.Competency;
import com.example.questionbank.domain.repository.CompetencyRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * In-memory implementation of the competency contract.
 */
public class InMemoryCompetencyRepository implements CompetencyRepository {

    private final Map<Long, Competency> store = new LinkedHashMap<>();
    private long sequence;

    @Override
    public Competency save(Competency competency) {
        if (competency.getId() == null) {
            competency.assignIdentity(++sequence);
        }
        store.put(competency.getId(), competency);
        return competency;
    }

    @Override
    public Optional<Competency> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Competency> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public List<Competency> findAllById(Collection<Long> ids) {
        return ids.stream()
                .map(store::get)
                .filter(java.util.Objects::nonNull)
                .toList();
    }
}
