package com.recruitcrm.candidatemanager.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruitcrm.candidatemanager.dto.CandidateRequest;
import com.recruitcrm.candidatemanager.entity.CandidateStatus;
import com.recruitcrm.candidatemanager.repository.CandidateRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Full-stack integration tests that boot the entire Spring context with an in-memory H2 database.
 * Validates end-to-end request → response flows including persistence.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CandidateIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CandidateRepository repository;

    @BeforeEach
    @Transactional
    void cleanDb() {
        repository.findAll().forEach(c -> repository.delete(c));
        repository.flush();
    }

    private CandidateRequest buildRequest(String firstName, String lastName, String email) {
        return CandidateRequest.builder()
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .phone("+1-555-0000")
                .currentCompany("TestCorp")
                .currentTitle("Engineer")
                .yearsOfExperience(5)
                .skills(List.of("Java", "Spring Boot"))
                .build();
    }

    @Test
    @DisplayName("Full CRUD lifecycle: create → read → update → delete")
    void fullCrudLifecycle() throws Exception {
        // CREATE
        CandidateRequest createReq = buildRequest("John", "Doe", "john@test.com");
        MvcResult createResult = mockMvc.perform(post("/api/v1/candidates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.status").value("NEW"))
                .andReturn();

        // Extract the created ID
        String responseJson = createResult.getResponse().getContentAsString();
        Long candidateId = objectMapper.readTree(responseJson).get("id").asLong();

        // READ
        mockMvc.perform(get("/api/v1/candidates/" + candidateId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("john@test.com"));

        // UPDATE
        CandidateRequest updateReq = CandidateRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@test.com")
                .currentTitle("Senior Engineer")
                .status(CandidateStatus.INTERVIEWING)
                .yearsOfExperience(6)
                .build();

        mockMvc.perform(put("/api/v1/candidates/" + candidateId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentTitle").value("Senior Engineer"))
                .andExpect(jsonPath("$.status").value("INTERVIEWING"));

        // DELETE
        mockMvc.perform(delete("/api/v1/candidates/" + candidateId))
                .andExpect(status().isNoContent());

        // Verify deletion
        mockMvc.perform(get("/api/v1/candidates/" + candidateId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Search and filter with multiple candidates")
    void searchAndFilter() throws Exception {
        // Seed two candidates
        CandidateRequest alice = CandidateRequest.builder()
                .firstName("Alice").lastName("Smith").email("alice@test.com")
                .currentCompany("TechCorp").currentTitle("Backend Dev")
                .yearsOfExperience(8).status(CandidateStatus.NEW)
                .skills(List.of("Java", "Spring Boot", "AWS"))
                .build();

        CandidateRequest bob = CandidateRequest.builder()
                .firstName("Bob").lastName("Jones").email("bob@test.com")
                .currentCompany("DataInc").currentTitle("Frontend Dev")
                .yearsOfExperience(3).status(CandidateStatus.INTERVIEWING)
                .skills(List.of("JavaScript", "React"))
                .build();

        mockMvc.perform(post("/api/v1/candidates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(alice)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/candidates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bob)))
                .andExpect(status().isCreated());

        // SEARCH by keyword "alice" — should find 1 result
        mockMvc.perform(get("/api/v1/candidates/search")
                        .param("keyword", "alice"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].firstName").value("Alice"));

        // FILTER by status = NEW — should find Alice only
        mockMvc.perform(get("/api/v1/candidates/filter")
                        .param("status", "NEW"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].firstName").value("Alice"));

        // FILTER by minExperience = 5 — should find Alice (8 yrs) only
        mockMvc.perform(get("/api/v1/candidates/filter")
                        .param("minExperience", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));

        // FILTER by skill = Spring Boot — should find Alice only (Bob has React, not Spring Boot)
        mockMvc.perform(get("/api/v1/candidates/filter")
                        .param("skill", "Spring Boot"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].firstName").value("Alice"));

        // FILTER by company = DataInc — should find Bob only
        mockMvc.perform(get("/api/v1/candidates/filter")
                        .param("currentCompany", "DataInc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].firstName").value("Bob"));
    }

    @Test
    @DisplayName("Pagination works correctly")
    void pagination() throws Exception {
        // Create 3 candidates
        for (int i = 1; i <= 3; i++) {
            CandidateRequest req = buildRequest("User" + i, "Test", "user" + i + "@test.com");
            mockMvc.perform(post("/api/v1/candidates")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isCreated());
        }

        // Request page 0 with size 2 — should get 2 items, totalElements = 3
        mockMvc.perform(get("/api/v1/candidates")
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    @Test
    @DisplayName("Duplicate email should return 409 Conflict")
    void duplicateEmail_returns409() throws Exception {
        CandidateRequest req = buildRequest("John", "Doe", "dup@test.com");

        // First create succeeds
        mockMvc.perform(post("/api/v1/candidates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());

        // Second create with same email fails
        mockMvc.perform(post("/api/v1/candidates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("already exists")));
    }

    @Test
    @DisplayName("Validation errors return 400 with field details")
    void validationErrors() throws Exception {
        // Missing required fields
        CandidateRequest invalid = CandidateRequest.builder().build();

        mockMvc.perform(post("/api/v1/candidates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.firstName").exists())
                .andExpect(jsonPath("$.details.lastName").exists())
                .andExpect(jsonPath("$.details.email").exists());
    }
}
