package com.example.questionbank.support;

import com.example.questionbank.domain.model.Topic;
import com.example.questionbank.domain.repository.TopicRepository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * In-memory implementation of the topic contract.
 */
public class InMemoryTopicRepository implements TopicRepository {

    private final Map<Long, Topic> store = new LinkedHashMap<>();
    private long sequence;

    @Override
    public Topic save(Topic topic) {
        if (topic.getId() == null) {
            topic.assignIdentity(++sequence);
        }
        store.put(topic.getId(), topic);
        return topic;
    }

    @Override
    public Optional<Topic> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Topic> findAll() {
        return new ArrayList<>(store.values());
    }
}
