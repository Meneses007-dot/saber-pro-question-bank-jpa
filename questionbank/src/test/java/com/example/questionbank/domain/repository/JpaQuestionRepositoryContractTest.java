package com.example.questionbank.domain.repository;

import com.example.questionbank.domain.model.Competency;
import com.example.questionbank.domain.model.Role;
import com.example.questionbank.domain.model.Topic;
import com.example.questionbank.domain.model.User;
import com.example.questionbank.infrastructure.jpa.CompetencyRepositoryJpa;
import com.example.questionbank.infrastructure.jpa.QuestionRepositoryJpa;
import com.example.questionbank.infrastructure.jpa.TopicRepositoryJpa;
import com.example.questionbank.infrastructure.jpa.UserRepositoryJpa;
import com.example.questionbank.support.DatabaseCleaner;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Runs the same repository contract against the JPA adapter on H2.
 */
@DataJpaTest
@Import({QuestionRepositoryJpa.class, UserRepositoryJpa.class,
        TopicRepositoryJpa.class, CompetencyRepositoryJpa.class})
class JpaQuestionRepositoryContractTest extends QuestionRepositoryContractTest {

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    private CompetencyRepository competencyRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Removes data committed by previous inherited tests.
     */
    @BeforeEach
    void cleanDatabase() {
        DatabaseCleaner.clean(jdbcTemplate);
    }

    @Override
    protected QuestionRepository questionRepository() {
        return questionRepository;
    }

    @Override
    protected Long createAuthor(String email) {
        return userRepository.save(new User(null, "Author", email, Role.AUTHOR)).getId();
    }

    @Override
    protected Long createTopic(String name) {
        return topicRepository.save(new Topic(null, name)).getId();
    }

    @Override
    protected Long createCompetency(String name) {
        return competencyRepository.save(new Competency(null, name, "Contract competency")).getId();
    }
}
