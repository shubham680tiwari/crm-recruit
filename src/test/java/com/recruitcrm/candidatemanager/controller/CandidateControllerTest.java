package com.recruitcrm.candidatemanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruitcrm.candidatemanager.dto.CandidateRequest;
import com.recruitcrm.candidatemanager.dto.CandidateResponse;
import com.recruitcrm.candidatemanager.entity.CandidateStatus;
import com.recruitcrm.candidatemanager.exception.CandidateNotFoundException;
import com.recruitcrm.candidatemanager.exception.DuplicateEmailException;
import com.recruitcrm.candidatemanager.service.CandidateService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Web-layer tests for CandidateController using MockMvc.
 * Validates HTTP status codes, JSON structure, and validation error responses.
 */
@WebMvcTest(CandidateController.class)
class CandidateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CandidateService candidateService;

    private CandidateRequest validRequest;
    private CandidateResponse sampleResponse;

    @BeforeEach
    void setUp() {
        validRequest = CandidateRequest.builder()
                .firstName("Alice")
                .lastName("Johnson")
                .email("alice@example.com")
                .phone("+1-555-0101")
                .currentCompany("TechCorp")
                .currentTitle("Senior Developer")
                .yearsOfExperience(8)
                .skills(List.of("Java", "Spring Boot"))
                .build();

        sampleResponse = CandidateResponse.builder()
                .id(1L)
                .firstName("Alice")
                .lastName("Johnson")
                .email("alice@example.com")
                .phone("+1-555-0101")
                .currentCompany("TechCorp")
                .currentTitle("Senior Developer")
                .status(CandidateStatus.NEW)
                .yearsOfExperience(8)
                .skills(List.of("Java", "Spring Boot"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // ---- POST /api/v1/candidates ----

    @Test
    @DisplayName("POST — should create candidate and return 201")
    void create_returns201() throws Exception {
        when(candidateService.create(any())).thenReturn(sampleResponse);

        mockMvc.perform(post("/api/v1/candidates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("alice@example.com"))
                .andExpect(jsonPath("$.status").value("NEW"));
    }

    @Test
    @DisplayName("POST — should return 400 for blank first name")
    void create_invalidFirstName_returns400() throws Exception {
        validRequest.setFirstName("");

        mockMvc.perform(post("/api/v1/candidates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.firstName").exists());
    }

    @Test
    @DisplayName("POST — should return 400 for invalid email format")
    void create_invalidEmail_returns400() throws Exception {
        validRequest.setEmail("not-an-email");

        mockMvc.perform(post("/api/v1/candidates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.email").exists());
    }

    @Test
    @DisplayName("POST — should return 409 for duplicate email")
    void create_duplicateEmail_returns409() throws Exception {
        when(candidateService.create(any())).thenThrow(new DuplicateEmailException("alice@example.com"));

        mockMvc.perform(post("/api/v1/candidates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("already exists")));
    }

    // ---- GET /api/v1/candidates/{id} ----

    @Test
    @DisplayName("GET by ID — should return 200 with candidate data")
    void getById_returns200() throws Exception {
        when(candidateService.getById(1L)).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/v1/candidates/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Alice"));
    }

    @Test
    @DisplayName("GET by ID — should return 404 for missing candidate")
    void getById_returns404() throws Exception {
        when(candidateService.getById(99L)).thenThrow(new CandidateNotFoundException(99L));

        mockMvc.perform(get("/api/v1/candidates/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("99")));
    }

    // ---- GET /api/v1/candidates ----

    @Test
    @DisplayName("GET all — should return paginated list")
    void getAll_returnsPaged() throws Exception {
        Page<CandidateResponse> page = new PageImpl<>(
                List.of(sampleResponse), PageRequest.of(0, 20), 1);

        when(candidateService.getAll(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/candidates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    // ---- PUT /api/v1/candidates/{id} ----

    @Test
    @DisplayName("PUT — should update and return 200")
    void update_returns200() throws Exception {
        when(candidateService.update(eq(1L), any())).thenReturn(sampleResponse);

        mockMvc.perform(put("/api/v1/candidates/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    // ---- DELETE /api/v1/candidates/{id} ----

    @Test
    @DisplayName("DELETE — should return 204 No Content")
    void delete_returns204() throws Exception {
        doNothing().when(candidateService).delete(1L);

        mockMvc.perform(delete("/api/v1/candidates/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE — should return 404 for missing candidate")
    void delete_returns404() throws Exception {
        doThrow(new CandidateNotFoundException(99L)).when(candidateService).delete(99L);

        mockMvc.perform(delete("/api/v1/candidates/99"))
                .andExpect(status().isNotFound());
    }

    // ---- GET /api/v1/candidates/search ----

    @Test
    @DisplayName("SEARCH — should return matching candidates")
    void search_returnsResults() throws Exception {
        Page<CandidateResponse> page = new PageImpl<>(
                List.of(sampleResponse), PageRequest.of(0, 20), 1);

        when(candidateService.search(eq("alice"), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/candidates/search")
                        .param("keyword", "alice"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].firstName").value("Alice"));
    }

    // ---- GET /api/v1/candidates/filter ----

    @Test
    @DisplayName("FILTER — should accept multiple criteria and return results")
    void filter_withCriteria() throws Exception {
        Page<CandidateResponse> page = new PageImpl<>(
                List.of(sampleResponse), PageRequest.of(0, 20), 1);

        when(candidateService.filter(any(), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/candidates/filter")
                        .param("status", "NEW")
                        .param("minExperience", "5")
                        .param("skill", "Java"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
    }

    // ---- Malformed input ----

    @Test
    @DisplayName("POST — should return 400 for malformed JSON body")
    void create_malformedJson_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/candidates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST — should return 400 for negative years of experience")
    void create_negativeExperience_returns400() throws Exception {
        validRequest.setYearsOfExperience(-1);

        mockMvc.perform(post("/api/v1/candidates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.yearsOfExperience").exists());
    }
}
