package com.example.questionbank.domain.repository;

import com.example.questionbank.domain.model.Topic;

import java.util.List;
import java.util.Optional;

/**
 * Persistence contract for question topics.
 */
public interface TopicRepository {

    /**
     * Inserts or updates a topic.
     *
     * @param topic topic to persist
     * @return the persisted topic
     */
    Topic save(Topic topic);

    /**
     * Finds a topic by identity.
     *
     * @param id topic identity
     * @return the topic when it exists
     */
    Optional<Topic> findById(Long id);

    /**
     * Lists every topic.
     *
     * @return all topics
     */
    List<Topic> findAll();
}
