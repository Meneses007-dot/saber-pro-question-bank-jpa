package com.example.questionbank.support;

import com.example.questionbank.domain.model.Quiz;
import com.example.questionbank.domain.repository.QuizRepository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * In-memory implementation of the quiz contract.
 */
public class InMemoryQuizRepository implements QuizRepository {

    private final Map<Long, Quiz> store = new LinkedHashMap<>();
    private long sequence;

    @Override
    public Quiz save(Quiz quiz) {
        if (quiz.getId() == null) {
            quiz.assignIdentity(++sequence);
        }
        store.put(quiz.getId(), quiz);
        return quiz;
    }

    @Override
    public Optional<Quiz> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Quiz> findAll() {
        return new ArrayList<>(store.values());
    }
}
