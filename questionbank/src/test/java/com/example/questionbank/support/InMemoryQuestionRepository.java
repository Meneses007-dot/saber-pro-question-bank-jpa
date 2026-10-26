package com.example.questionbank.support;

import com.example.questionbank.domain.model.Question;
import com.example.questionbank.domain.model.QuestionStatus;
import com.example.questionbank.domain.repository.QuestionRepository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * In-memory implementation of the question contract used by domain and
 * application tests that must not depend on JPA.
 */
public class InMemoryQuestionRepository implements QuestionRepository {

    private final Map<Long, Question> store = new LinkedHashMap<>();
    private long sequence;

    @Override
    public Question save(Question question) {
        if (question.getId() == null) {
            question.assignIdentity(++sequence);
        }
        store.put(question.getId(), question);
        return question;
    }

    @Override
    public Optional<Question> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Question> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public List<Question> findByAuthorId(Long authorId) {
        return store.values().stream()
                .filter(question -> question.getAuthorId().equals(authorId))
                .toList();
    }

    @Override
    public List<Question> findByStatus(QuestionStatus status) {
        return store.values().stream()
                .filter(question -> question.getStatus() == status)
                .toList();
    }

    @Override
    public List<Question> findPublishedByCompetencyId(Long competencyId) {
        return store.values().stream()
                .filter(Question::isPublished)
                .filter(question -> question.getCompetencyIds().contains(competencyId))
                .toList();
    }
}
