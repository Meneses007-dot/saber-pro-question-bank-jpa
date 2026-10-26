package com.example.questionbank.infrastructure.jpa;

import com.example.questionbank.domain.model.Question;
import com.example.questionbank.domain.model.QuestionStatus;
import com.example.questionbank.domain.repository.QuestionRepository;
import com.example.questionbank.domain.exception.ResourceNotFoundException;
import com.example.questionbank.infrastructure.jpa.mapper.QuestionMapper;
import com.example.questionbank.infrastructure.jpa.record.CompetencyRecord;
import com.example.questionbank.infrastructure.jpa.record.QuestionRecord;
import com.example.questionbank.infrastructure.jpa.record.TopicRecord;
import com.example.questionbank.infrastructure.jpa.record.UserRecord;
import com.example.questionbank.infrastructure.jpa.repository.SpringCompetencyDataRepository;
import com.example.questionbank.infrastructure.jpa.repository.SpringQuestionDataRepository;
import com.example.questionbank.infrastructure.jpa.repository.SpringTopicDataRepository;
import com.example.questionbank.infrastructure.jpa.repository.SpringUserDataRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * JPA implementation of the {@link QuestionRepository} contract.
 *
 * <p>This adapter resolves the referenced aggregates, distinguishes inserts
 * from updates and delegates translation to {@link QuestionMapper}.</p>
 */
@Repository
public class QuestionRepositoryJpa implements QuestionRepository {

    private final SpringQuestionDataRepository questions;
    private final SpringUserDataRepository users;
    private final SpringTopicDataRepository topics;
    private final SpringCompetencyDataRepository competencies;

    /**
     * Creates the adapter.
     *
     * @param questions Spring Data question repository
     * @param users Spring Data user repository
     * @param topics Spring Data topic repository
     * @param competencies Spring Data competency repository
     */
    public QuestionRepositoryJpa(SpringQuestionDataRepository questions, SpringUserDataRepository users,
                                 SpringTopicDataRepository topics,
                                 SpringCompetencyDataRepository competencies) {
        this.questions = questions;
        this.users = users;
        this.topics = topics;
        this.competencies = competencies;
    }

    @Override
    @Transactional
    public Question save(Question question) {
        UserRecord author = users.findById(question.getAuthorId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Author not found: " + question.getAuthorId()));
        TopicRecord topic = topics.findById(question.getTopicId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Topic not found: " + question.getTopicId()));
        Set<CompetencyRecord> competencyRecords = new LinkedHashSet<>(
                competencies.findAllById(question.getCompetencyIds()));
        if (competencyRecords.size() != question.getCompetencyIds().size()) {
            throw new ResourceNotFoundException(
                    "One or more competencies were not found: " + question.getCompetencyIds());
        }

        QuestionRecord record;
        if (question.getId() == null) {
            record = QuestionMapper.toNewRecord(question, author, topic, competencyRecords);
        } else {
            record = questions.findById(question.getId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Question not found: " + question.getId()));
            QuestionMapper.updateRecord(record, question, author, topic, competencyRecords);
        }
        return QuestionMapper.toDomain(questions.save(record));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Question> findById(Long id) {
        return questions.findById(id).map(QuestionMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Question> findAll() {
        return questions.findAll().stream().map(QuestionMapper::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Question> findByAuthorId(Long authorId) {
        return questions.findByAuthorId(authorId).stream().map(QuestionMapper::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Question> findByStatus(QuestionStatus status) {
        return questions.findByStatus(status).stream().map(QuestionMapper::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Question> findPublishedByCompetencyId(Long competencyId) {
        return questions.findByCompetencyIdAndStatus(competencyId, QuestionStatus.PUBLISHED)
                .stream().map(QuestionMapper::toDomain).toList();
    }
}
