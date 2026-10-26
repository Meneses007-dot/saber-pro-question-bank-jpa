package com.example.questionbank.infrastructure.jpa.repository;

import com.example.questionbank.infrastructure.jpa.record.TopicRecord;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data repository used internally by the topic repository adapter.
 */
public interface SpringTopicDataRepository extends JpaRepository<TopicRecord, Long> {
}
