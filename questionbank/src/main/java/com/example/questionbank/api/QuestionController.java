package com.example.questionbank.api;

import com.example.questionbank.application.QuestionService;
import com.example.questionbank.application.ReviewService;
import com.example.questionbank.domain.model.Question;
import com.example.questionbank.domain.model.QuestionStatus;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST endpoints for the question use cases and the review workflow.
 */
@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    private final QuestionService questionService;
    private final ReviewService reviewService;

    /**
     * Creates the controller.
     *
     * @param questionService question use cases
     * @param reviewService review workflow use cases
     */
    public QuestionController(QuestionService questionService, ReviewService reviewService) {
        this.questionService = questionService;
        this.reviewService = reviewService;
    }

    /**
     * Creates a draft question.
     *
     * @param request creation data
     * @return the created question
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public QuestionResponse createQuestion(@RequestBody QuestionRequest request) {
        return QuestionResponse.from(questionService.createQuestion(request.toCommand()));
    }

    /**
     * Retrieves a question by identity.
     *
     * @param id question identity
     * @return the question
     */
    @GetMapping("/{id}")
    public QuestionResponse getQuestion(@PathVariable Long id) {
        return QuestionResponse.from(questionService.getQuestion(id));
    }

    /**
     * Lists questions by author, state or competency.
     *
     * <p>When {@code competencyId} is present only published questions are
     * returned, which is the query required by the statement.</p>
     *
     * @param authorId optional author identity
     * @param status optional lifecycle state
     * @param competencyId optional competency identity
     * @return matching questions
     */
    @GetMapping
    public List<QuestionResponse> listQuestions(@RequestParam(required = false) Long authorId,
                                                @RequestParam(required = false) QuestionStatus status,
                                                @RequestParam(required = false) Long competencyId) {
        List<Question> questions = competencyId != null
                ? questionService.findPublishedByCompetency(competencyId)
                : questionService.listQuestions(authorId, status);
        return questions.stream().map(QuestionResponse::from).toList();
    }

    /**
     * Replaces the classification of a question.
     *
     * @param id question identity
     * @param request new topic and competencies
     * @return the updated question
     */
    @PutMapping("/{id}/classification")
    public QuestionResponse classifyQuestion(@PathVariable Long id,
                                             @RequestBody ClassifyQuestionRequest request) {
        return QuestionResponse.from(questionService.classifyQuestion(id, request.toCommand()));
    }

    /**
     * Sends a draft question to review.
     *
     * @param id question identity
     * @return the updated question
     */
    @PostMapping("/{id}/submit")
    public QuestionResponse submitForReview(@PathVariable Long id) {
        return QuestionResponse.from(questionService.submitForReview(id));
    }

    /**
     * Starts the review of a pending question.
     *
     * @param id question identity
     * @return the updated question
     */
    @PostMapping("/{id}/review/start")
    public QuestionResponse startReview(@PathVariable Long id) {
        return QuestionResponse.from(reviewService.startReview(id));
    }

    /**
     * Approves a question that is being reviewed.
     *
     * @param id question identity
     * @return the updated question
     */
    @PostMapping("/{id}/review/approve")
    public QuestionResponse approve(@PathVariable Long id) {
        return QuestionResponse.from(reviewService.approve(id));
    }

    /**
     * Rejects a question and returns it to draft.
     *
     * @param id question identity
     * @return the updated question
     */
    @PostMapping("/{id}/review/reject")
    public QuestionResponse reject(@PathVariable Long id) {
        return QuestionResponse.from(reviewService.reject(id));
    }

    /**
     * Publishes an approved question.
     *
     * @param id question identity
     * @return the updated question
     */
    @PostMapping("/{id}/publish")
    public QuestionResponse publish(@PathVariable Long id) {
        return QuestionResponse.from(reviewService.publish(id));
    }

    /**
     * Archives a question.
     *
     * @param id question identity
     * @return the updated question
     */
    @PostMapping("/{id}/archive")
    public QuestionResponse archive(@PathVariable Long id) {
        return QuestionResponse.from(reviewService.archive(id));
    }
}
