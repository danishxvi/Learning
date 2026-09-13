package com.danish.spring.capstone;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// A full, real @SpringBootTest - real H2 database, real Spring Security filter chain,
// real JWT generation and validation - driven through MockMvc rather than a live
// HTTP client, exactly like lesson 49's full integration tests.
@SpringBootTest
@AutoConfigureMockMvc
class TaskApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void unauthenticatedRequestIsRejected() throws Exception {
        mockMvc.perform(get("/tasks"))
                .andExpect(status().isForbidden());
    }

    @Test
    void registerLoginCreateAndRetrieveTask() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType("application/json")
                        .content("{\"username\":\"carol\",\"password\":\"carolpass1\"}"))
                .andExpect(status().isCreated());

        String loginResponse = mockMvc.perform(post("/auth/login")
                        .contentType("application/json")
                        .content("{\"username\":\"carol\",\"password\":\"carolpass1\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String token = objectMapper.readTree(loginResponse).get("token").asText();

        String createResponse = mockMvc.perform(post("/tasks")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content("{\"title\":\"Test task\",\"status\":\"TODO\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Test task"))
                .andExpect(jsonPath("$.status").value("TODO"))
                .andReturn().getResponse().getContentAsString();
        long taskId = objectMapper.readTree(createResponse).get("id").asLong();

        // NOT a hardcoded "/tasks/1" - @SpringBootTest methods share ONE Spring context
        // and ONE in-memory H2 database across the whole test class by default, so a
        // task created by an earlier test method in this same run can easily claim id
        // 1 first. The first version of this test hardcoded /tasks/1 and failed with a
        // real 404 once another test's task took that id - using the actual id this
        // test just created is what makes it correct regardless of execution order.
        mockMvc.perform(get("/tasks/" + taskId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test task"));
    }

    @Test
    void secondUserCannotSeeFirstUsersTask() throws Exception {
        mockMvc.perform(post("/auth/register")
                .contentType("application/json")
                .content("{\"username\":\"dave\",\"password\":\"davepass1\"}"));
        mockMvc.perform(post("/auth/register")
                .contentType("application/json")
                .content("{\"username\":\"erin\",\"password\":\"erinpass1\"}"));

        String daveToken = extractToken(mockMvc.perform(post("/auth/login")
                        .contentType("application/json")
                        .content("{\"username\":\"dave\",\"password\":\"davepass1\"}"))
                .andReturn().getResponse().getContentAsString());

        String erinToken = extractToken(mockMvc.perform(post("/auth/login")
                        .contentType("application/json")
                        .content("{\"username\":\"erin\",\"password\":\"erinpass1\"}"))
                .andReturn().getResponse().getContentAsString());

        String createResponse = mockMvc.perform(post("/tasks")
                        .header("Authorization", "Bearer " + daveToken)
                        .contentType("application/json")
                        .content("{\"title\":\"Dave's private task\",\"status\":\"TODO\"}"))
                .andReturn().getResponse().getContentAsString();
        long taskId = objectMapper.readTree(createResponse).get("id").asLong();

        mockMvc.perform(get("/tasks/" + taskId)
                        .header("Authorization", "Bearer " + erinToken))
                .andExpect(status().isNotFound());
    }

    private String extractToken(String json) throws Exception {
        return objectMapper.readTree(json).get("token").asText();
    }
}
