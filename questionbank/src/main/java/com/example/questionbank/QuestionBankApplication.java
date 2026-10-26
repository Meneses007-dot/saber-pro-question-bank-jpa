package com.example.questionbank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Boots the Saber Pro question bank application.
 *
 * <p>The application is organized in layers: {@code api} receives HTTP requests,
 * {@code application} coordinates use cases, {@code domain} owns the business
 * model and repository contracts, and {@code infrastructure} implements those
 * contracts with JPA.</p>
 */
@SpringBootApplication
public class QuestionBankApplication {

    /**
     * Starts the Spring Boot application.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(QuestionBankApplication.class, args);
    }
}
