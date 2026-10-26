package com.example.questionbank.infrastructure.jpa.repository;

import com.example.questionbank.domain.model.QuestionStatus;
import com.example.questionbank.infrastructure.jpa.record.QuestionRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Spring Data repository used internally by the question repository adapter.
 */
public interface SpringQuestionDataRepository extends JpaRepository<QuestionRecord, Long> {

    /**
     * Finds the questions of an author.
     *
     * @param authorId author identity
     * @return matching records
     */
    List<QuestionRecord> findByAuthorId(Long authorId);

    /**
     * Finds the questions in a lifecycle state.
     *
     * @param status lifecycle state
     * @return matching records
     */
    List<QuestionRecord> findByStatus(QuestionStatus status);

    /**
     * Finds the questions of a competency filtered by state.
     *
     * @param competencyId competency identity
     * @param status required lifecycle state
     * @return matching records
     */
    @Query("select distinct q from QuestionRecord q join q.competencies c "
            + "where c.id = :competencyId and q.status = :status")
    List<QuestionRecord> findByCompetencyIdAndStatus(@Param("competencyId") Long competencyId,
                                                     @Param("status") QuestionStatus status);
}
