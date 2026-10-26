package com.example.questionbank.api;

import java.time.Instant;

/**
 * Uniform error body returned by the API.
 *
 * @param timestamp moment of the failure
 * @param status HTTP status code
 * @param error HTTP reason phrase
 * @param message explanation of the failure
 * @param path requested path
 */
public record ErrorResponse(Instant timestamp, int status, String error, String message, String path) {
}
