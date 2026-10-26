package com.example.questionbank.domain.repository;

import com.example.questionbank.domain.model.Competency;
import com.example.questionbank.domain.model.Role;
import com.example.questionbank.domain.model.Topic;
import com.example.questionbank.domain.model.User;
import com.example.questionbank.support.InMemoryCompetencyRepository;
import com.example.questionbank.support.InMemoryQuestionRepository;
import com.example.questionbank.support.InMemoryTopicRepository;
import com.example.questionbank.support.InMemoryUserRepository;

/**
 * Runs the repository contract against the in-memory fake, without Spring or
 * JPA.
 */
class InMemoryQuestionRepositoryContractTest extends QuestionRepositoryContractTest {

    private final InMemoryQuestionRepository questions = new InMemoryQuestionRepository();
    private final InMemoryUserRepository users = new InMemoryUserRepository();
    private final InMemoryTopicRepository topics = new InMemoryTopicRepository();
    private final InMemoryCompetencyRepository competencies = new InMemoryCompetencyRepository();

    @Override
    protected InMemoryQuestionRepository questionRepository() {
        return questions;
    }

    @Override
    protected Long createAuthor(String email) {
        return users.save(new User(null, "Author", email, Role.AUTHOR)).getId();
    }

    @Override
    protected Long createTopic(String name) {
        return topics.save(new Topic(null, name)).getId();
    }

    @Override
    protected Long createCompetency(String name) {
        return competencies.save(new Competency(null, name, "Contract competency")).getId();
    }
}
