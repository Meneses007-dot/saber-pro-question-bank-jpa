package com.example.questionbank.infrastructure.jpa.mapper;

import com.example.questionbank.domain.model.Topic;
import com.example.questionbank.infrastructure.jpa.record.TopicRecord;

/**
 * Translates topics between the domain and the JPA model.
 */
public final class TopicMapper {

    private TopicMapper() {
    }

    /**
     * Converts a JPA record to a domain topic.
     *
     * @param record JPA record
     * @return domain topic
     */
    public static Topic toDomain(TopicRecord record) {
        return new Topic(record.getId(), record.getName());
    }

    /**
     * Creates a new JPA record from a domain topic.
     *
     * @param topic domain topic
     * @return new JPA record
     */
    public static TopicRecord toNewRecord(Topic topic) {
        return new TopicRecord(topic.getName());
    }
}
