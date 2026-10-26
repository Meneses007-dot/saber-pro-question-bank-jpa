package com.example.questionbank.infrastructure.jpa;

import com.example.questionbank.domain.exception.ResourceNotFoundException;
import com.example.questionbank.domain.model.Topic;
import com.example.questionbank.domain.repository.TopicRepository;
import com.example.questionbank.infrastructure.jpa.mapper.TopicMapper;
import com.example.questionbank.infrastructure.jpa.record.TopicRecord;
import com.example.questionbank.infrastructure.jpa.repository.SpringTopicDataRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * JPA implementation of the {@link TopicRepository} contract.
 */
@Repository
public class TopicRepositoryJpa implements TopicRepository {

    private final SpringTopicDataRepository data;

    /**
     * Creates the adapter.
     *
     * @param data Spring Data topic repository
     */
    public TopicRepositoryJpa(SpringTopicDataRepository data) {
        this.data = data;
    }

    @Override
    @Transactional
    public Topic save(Topic topic) {
        if (topic.getId() == null) {
            return TopicMapper.toDomain(data.save(TopicMapper.toNewRecord(topic)));
        }
        TopicRecord record = data.findById(topic.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found: " + topic.getId()));
        record.update(topic.getName());
        return TopicMapper.toDomain(data.save(record));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Topic> findById(Long id) {
        return data.findById(id).map(TopicMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Topic> findAll() {
        return data.findAll().stream().map(TopicMapper::toDomain).toList();
    }
}
