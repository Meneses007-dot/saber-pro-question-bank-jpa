package com.example.questionbank.api;

import com.example.questionbank.domain.model.Competency;
import com.example.questionbank.domain.model.Role;
import com.example.questionbank.domain.model.Topic;
import com.example.questionbank.domain.model.User;
import com.example.questionbank.domain.repository.CompetencyRepository;
import com.example.questionbank.domain.repository.TopicRepository;
import com.example.questionbank.domain.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * HTTP tests of the question endpoints and the global exception handler.
 */
@SpringBootTest
@AutoConfigureMockMvc
class QuestionApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository users;

    @Autowired
    private TopicRepository topics;

    @Autowired
    private CompetencyRepository competencies;

    private Long authorId;
    private Long teacherId;
    private Long topicId;
    private Long competencyId;

    @BeforeEach
    void setUp() {
        String suffix = UUID.randomUUID().toString();
        authorId = users.save(new User(null, "Author " + suffix, "author-" + suffix + "@api.edu",
                Role.AUTHOR)).getId();
        teacherId = users.save(new User(null, "Teacher " + suffix, "teacher-" + suffix + "@api.edu",
                Role.TEACHER)).getId();
        topicId = topics.save(new Topic(null, "Topic " + suffix)).getId();
        competencyId = competencies.save(new Competency(null, "Competency " + suffix, "Description")).getId();
    }

    private String questionJson(boolean withCompetency) {
        String competencyIds = withCompetency ? "[" + competencyId + "]" : "[]";
        return """
                {
                  "statement": "Which strategy uses a base table?",
                  "authorId": %d,
                  "topicId": %d,
                  "competencyIds": %s,
                  "type": "MULTIPLE_CHOICE",
                  "difficulty": "MEDIUM",
                  "options": [
                    {"text": "JOINED", "correct": true},
                    {"text": "SINGLE_TABLE", "correct": false},
                    {"text": "TABLE_PER_CLASS", "correct": false},
                    {"text": "NONE", "correct": false},
                    {"text": "ALL", "correct": false}
                  ]
                }
                """.formatted(authorId, topicId, competencyIds);
    }

    private long createQuestion(boolean withCompetency) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(questionJson(withCompetency)))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        return body.get("id").asLong();
    }

    @Test
    void createsAQuestionAndReturns201() throws Exception {
        mockMvc.perform(post("/api/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(questionJson(true)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.type").value("MULTIPLE_CHOICE"))
                .andExpect(jsonPath("$.options.length()").value(5));
    }

    @Test
    void returns404ForAnUnknownQuestion() throws Exception {
        mockMvc.perform(get("/api/questions/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void returns409WhenPublishingADraftQuestion() throws Exception {
        long questionId = createQuestion(true);

        mockMvc.perform(post("/api/questions/" + questionId + "/publish"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("DRAFT")));
    }

    @Test
    void returns422WhenSubmittingAnIncompleteQuestion() throws Exception {
        long questionId = createQuestion(false);

        mockMvc.perform(post("/api/questions/" + questionId + "/submit"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("competency")));
    }

    @Test
    void returns422WhenCreatingAQuizWithADraftQuestion() throws Exception {
        long questionId = createQuestion(true);
        String body = """
                {"title": "Invalid quiz %s", "createdById": %d, "questionIds": [%d]}
                """.formatted(UUID.randomUUID(), teacherId, questionId);

        mockMvc.perform(post("/api/quizzes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422));
    }
}
