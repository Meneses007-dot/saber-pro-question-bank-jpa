package com.example.questionbank.api;

import com.example.questionbank.application.QuizService;
import com.example.questionbank.domain.model.Quiz;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST endpoints for quizzes.
 */
@RestController
@RequestMapping("/api/quizzes")
public class QuizController {

    private final QuizService quizService;

    /**
     * Creates the controller.
     *
     * @param quizService quiz use cases
     */
    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    /**
     * Creates a quiz from published questions.
     *
     * @param request creation data
     * @return the created quiz
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public QuizResponse createQuiz(@RequestBody QuizRequest request) {
        return QuizResponse.from(quizService.createQuiz(request.toCommand()));
    }

    /**
     * Retrieves a quiz by identity.
     *
     * @param id quiz identity
     * @return the quiz
     */
    @GetMapping("/{id}")
    public QuizResponse getQuiz(@PathVariable Long id) {
        return QuizResponse.from(quizService.getQuiz(id));
    }

    /**
     * Lists every quiz.
     *
     * @return all quizzes
     */
    @GetMapping
    public List<QuizResponse> listQuizzes() {
        return quizService.listQuizzes().stream().map(QuizResponse::from).toList();
    }
}
