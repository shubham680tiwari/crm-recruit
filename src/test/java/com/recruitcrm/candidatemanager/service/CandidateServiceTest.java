package com.recruitcrm.candidatemanager.service;

import com.recruitcrm.candidatemanager.dto.CandidateMapper;
import com.recruitcrm.candidatemanager.dto.CandidateRequest;
import com.recruitcrm.candidatemanager.dto.CandidateResponse;
import com.recruitcrm.candidatemanager.dto.CandidateSearchCriteria;
import com.recruitcrm.candidatemanager.entity.Candidate;
import com.recruitcrm.candidatemanager.entity.CandidateStatus;
import com.recruitcrm.candidatemanager.exception.CandidateNotFoundException;
import com.recruitcrm.candidatemanager.exception.DuplicateEmailException;
import com.recruitcrm.candidatemanager.repository.CandidateRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CandidateService.
 * Uses Mockito to isolate business logic from the database layer.
 */
@ExtendWith(MockitoExtension.class)
class CandidateServiceTest {

    @Mock
    private CandidateRepository repository;

    @Mock
    private CandidateMapper mapper;

    @InjectMocks
    private CandidateService service;

    private Candidate sampleEntity;
    private CandidateRequest sampleRequest;
    private CandidateResponse sampleResponse;

    @BeforeEach
    void setUp() {
        sampleEntity = Candidate.builder()
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

        sampleRequest = CandidateRequest.builder()
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

    // ---- CREATE ----

    @Test
    @DisplayName("create — should persist and return a new candidate")
    void create_success() {
        when(repository.existsByEmailIgnoreCase("alice@example.com")).thenReturn(false);
        when(mapper.toEntity(sampleRequest)).thenReturn(sampleEntity);
        when(repository.save(sampleEntity)).thenReturn(sampleEntity);
        when(mapper.toResponse(sampleEntity)).thenReturn(sampleResponse);

        CandidateResponse result = service.create(sampleRequest);

        assertThat(result.getEmail()).isEqualTo("alice@example.com");
        verify(repository).save(sampleEntity);
    }

    @Test
    @DisplayName("create — should throw DuplicateEmailException for existing email")
    void create_duplicateEmail() {
        when(repository.existsByEmailIgnoreCase("alice@example.com")).thenReturn(true);

        assertThatThrownBy(() -> service.create(sampleRequest))
                .isInstanceOf(DuplicateEmailException.class);
        verify(repository, never()).save(any());
    }

    // ---- GET BY ID ----

    @Test
    @DisplayName("getById — should return candidate when found")
    void getById_found() {
        when(repository.findById(1L)).thenReturn(Optional.of(sampleEntity));
        when(mapper.toResponse(sampleEntity)).thenReturn(sampleResponse);

        CandidateResponse result = service.getById(1L);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getById — should throw CandidateNotFoundException when not found")
    void getById_notFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(99L))
                .isInstanceOf(CandidateNotFoundException.class)
                .hasMessageContaining("99");
    }

    // ---- GET ALL ----

    @Test
    @DisplayName("getAll — should return paginated results")
    void getAll_paged() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Candidate> page = new PageImpl<>(List.of(sampleEntity), pageable, 1);

        when(repository.findAll(pageable)).thenReturn(page);
        when(mapper.toResponse(sampleEntity)).thenReturn(sampleResponse);

        Page<CandidateResponse> result = service.getAll(pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    // ---- UPDATE ----

    @Test
    @DisplayName("update — should merge changes and persist")
    void update_success() {
        CandidateRequest updateRequest = CandidateRequest.builder()
                .firstName("Alice")
                .lastName("Johnson-Smith")
                .email("alice@example.com")
                .build();

        when(repository.findById(1L)).thenReturn(Optional.of(sampleEntity));
        when(repository.save(sampleEntity)).thenReturn(sampleEntity);
        when(mapper.toResponse(sampleEntity)).thenReturn(sampleResponse);

        CandidateResponse result = service.update(1L, updateRequest);

        verify(mapper).updateEntityFromRequest(updateRequest, sampleEntity);
        verify(repository).save(sampleEntity);
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("update — should throw if new email is already taken by another candidate")
    void update_duplicateEmail() {
        CandidateRequest updateRequest = CandidateRequest.builder()
                .email("bob@example.com")
                .firstName("Alice")
                .lastName("Johnson")
                .build();

        when(repository.findById(1L)).thenReturn(Optional.of(sampleEntity));
        when(repository.existsByEmailIgnoreCase("bob@example.com")).thenReturn(true);

        assertThatThrownBy(() -> service.update(1L, updateRequest))
                .isInstanceOf(DuplicateEmailException.class);
    }

    @Test
    @DisplayName("update — should throw CandidateNotFoundException for missing ID")
    void update_notFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(99L, sampleRequest))
                .isInstanceOf(CandidateNotFoundException.class);
    }

    // ---- DELETE ----

    @Test
    @DisplayName("delete — should remove the candidate when found")
    void delete_success() {
        when(repository.findById(1L)).thenReturn(Optional.of(sampleEntity));

        service.delete(1L);

        verify(repository).delete(sampleEntity);
    }

    @Test
    @DisplayName("delete — should throw CandidateNotFoundException when not found")
    void delete_notFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(99L))
                .isInstanceOf(CandidateNotFoundException.class);
    }

    // ---- SEARCH ----

    @Test
    @DisplayName("search — should delegate keyword search to repository")
    void search_byKeyword() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Candidate> page = new PageImpl<>(List.of(sampleEntity), pageable, 1);

        when(repository.searchByKeyword("alice", pageable)).thenReturn(page);
        when(mapper.toResponse(sampleEntity)).thenReturn(sampleResponse);

        Page<CandidateResponse> result = service.search("alice", pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    // ---- FILTER ----

    @Test
    @DisplayName("filter — should apply specification-based filtering")
    @SuppressWarnings("unchecked")
    void filter_withCriteria() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Candidate> page = new PageImpl<>(List.of(sampleEntity), pageable, 1);

        CandidateSearchCriteria criteria = CandidateSearchCriteria.builder()
                .status(CandidateStatus.NEW)
                .minExperience(5)
                .build();

        when(repository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(mapper.toResponse(sampleEntity)).thenReturn(sampleResponse);

        Page<CandidateResponse> result = service.filter(criteria, pageable);

        assertThat(result.getContent()).hasSize(1);
    }
}
