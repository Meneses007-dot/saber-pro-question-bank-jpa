package com.example.questionbank.api;

import com.example.questionbank.domain.exception.DomainException;
import com.example.questionbank.domain.exception.InvalidStateTransitionException;
import com.example.questionbank.domain.exception.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;

/**
 * Translates application and domain failures into coherent HTTP responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Maps a missing resource to {@code 404 Not Found}.
     *
     * @param exception failure raised by the application layer
     * @param request current request
     * @return error body
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException exception,
                                                        HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, exception.getMessage(), request);
    }

    /**
     * Maps an illegal lifecycle transition to {@code 409 Conflict}.
     *
     * @param exception failure raised by the domain
     * @param request current request
     * @return error body
     */
    @ExceptionHandler(InvalidStateTransitionException.class)
    public ResponseEntity<ErrorResponse> handleInvalidTransition(InvalidStateTransitionException exception,
                                                                  HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, exception.getMessage(), request);
    }

    /**
     * Maps a violated business rule to {@code 422 Unprocessable Entity}.
     *
     * @param exception failure raised by the domain
     * @param request current request
     * @return error body
     */
    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ErrorResponse> handleDomainException(DomainException exception,
                                                               HttpServletRequest request) {
        return build(HttpStatus.UNPROCESSABLE_ENTITY, exception.getMessage(), request);
    }

    /**
     * Maps malformed bodies or parameters to {@code 400 Bad Request}.
     *
     * @param exception parsing failure
     * @param request current request
     * @return error body
     */
    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
            IllegalArgumentException.class})
    public ResponseEntity<ErrorResponse> handleBadRequest(Exception exception, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, exception.getMessage(), request);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message, HttpServletRequest request) {
        ErrorResponse body = new ErrorResponse(Instant.now(), status.value(), status.getReasonPhrase(),
                message, request.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }
}
