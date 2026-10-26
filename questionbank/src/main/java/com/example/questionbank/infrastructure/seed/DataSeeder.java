package com.example.questionbank.infrastructure.seed;

import com.example.questionbank.domain.model.Competency;
import com.example.questionbank.domain.model.Role;
import com.example.questionbank.domain.model.Topic;
import com.example.questionbank.domain.model.User;
import com.example.questionbank.domain.repository.CompetencyRepository;
import com.example.questionbank.domain.repository.TopicRepository;
import com.example.questionbank.domain.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Loads a small catalog of users, topics and competencies so the REST API can
 * be explored with the curl examples of the README.
 *
 * <p>The seeder is disabled in tests through
 * {@code questionbank.seed.enabled=false}.</p>
 */
@Component
@ConditionalOnProperty(name = "questionbank.seed.enabled", havingValue = "true", matchIfMissing = true)
public class DataSeeder implements CommandLineRunner {

    private final UserRepository users;
    private final TopicRepository topics;
    private final CompetencyRepository competencies;

    /**
     * Creates the seeder.
     *
     * @param users user repository
     * @param topics topic repository
     * @param competencies competency repository
     */
    public DataSeeder(UserRepository users, TopicRepository topics, CompetencyRepository competencies) {
        this.users = users;
        this.topics = topics;
        this.competencies = competencies;
    }

    @Override
    public void run(String... args) {
        if (!users.findAll().isEmpty()) {
            return;
        }
        users.save(new User(null, "Ana Administrator", "admin@questionbank.edu", Role.ADMIN));
        users.save(new User(null, "Arturo Author", "author@questionbank.edu", Role.AUTHOR));
        users.save(new User(null, "Rita Reviewer", "reviewer@questionbank.edu", Role.REVIEWER));
        users.save(new User(null, "Tomas Teacher", "teacher@questionbank.edu", Role.TEACHER));
        users.save(new User(null, "Sara Student", "student@questionbank.edu", Role.STUDENT));

        topics.save(new Topic(null, "Software Architecture"));
        topics.save(new Topic(null, "Databases"));

        competencies.save(new Competency(null, "Solution Design",
                "Ability to design software solutions"));
        competencies.save(new Competency(null, "Problem Analysis",
                "Ability to analyze problems and constraints"));
        competencies.save(new Competency(null, "Persistence",
                "Knowledge of data persistence and transactions"));
    }
}
