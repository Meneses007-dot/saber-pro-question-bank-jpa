package com.example.questionbank.application;

import com.example.questionbank.domain.exception.ResourceNotFoundException;
import com.example.questionbank.domain.model.Question;
import com.example.questionbank.domain.repository.QuestionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Coordinates the review workflow of a question.
 *
 * <p>The service only loads the aggregate and delegates the transition to the
 * domain methods, which validate the state machine.</p>
 */
@Service
@Transactional
public class ReviewService {

    private final QuestionRepository questions;

    /**
     * Injects the question repository contract.
     *
     * @param questions question repository
     */
    public ReviewService(QuestionRepository questions) {
        this.questions = questions;
    }

    /**
     * Starts the review of a pending question.
     *
     * @param questionId question identity
     * @return the updated question
     */
    public Question startReview(Long questionId) {
        Question question = findQuestion(questionId);
        question.startReview();
        return questions.save(question);
    }

    /**
     * Approves a question that is being reviewed.
     *
     * @param questionId question identity
     * @return the updated question
     */
    public Question approve(Long questionId) {
        Question question = findQuestion(questionId);
        question.approve();
        return questions.save(question);
    }

    /**
     * Rejects a question and returns it to draft.
     *
     * @param questionId question identity
     * @return the updated question
     */
    public Question reject(Long questionId) {
        Question question = findQuestion(questionId);
        question.reject();
        return questions.save(question);
    }

    /**
     * Publishes an approved question.
     *
     * @param questionId question identity
     * @return the updated question
     */
    public Question publish(Long questionId) {
        Question question = findQuestion(questionId);
        question.publish();
        return questions.save(question);
    }

    /**
     * Archives a question.
     *
     * @param questionId question identity
     * @return the updated question
     */
    public Question archive(Long questionId) {
        Question question = findQuestion(questionId);
        question.archive();
        return questions.save(question);
    }

    private Question findQuestion(Long id) {
        return questions.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found: " + id));
    }
}
