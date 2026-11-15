package com.recruitcrm.candidatemanager.controller;

import com.recruitcrm.candidatemanager.dto.*;
import com.recruitcrm.candidatemanager.entity.CandidateStatus;
import com.recruitcrm.candidatemanager.service.CandidateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller exposing CRUD, search, and filter endpoints for candidates.
 * All endpoints are versioned under /api/v1 for forward compatibility.
 */
@RestController
@RequestMapping("/api/v1/candidates")
@RequiredArgsConstructor
@Tag(name = "Candidates", description = "Candidate management operations")
public class CandidateController {

    private final CandidateService candidateService;

    @PostMapping
    @Operation(summary = "Create a new candidate")
    public ResponseEntity<CandidateResponse> create(@Valid @RequestBody CandidateRequest request) {
        CandidateResponse response = candidateService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a candidate by ID")
    public ResponseEntity<CandidateResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(candidateService.getById(id));
    }

    @GetMapping
    @Operation(summary = "List all candidates with pagination")
    public ResponseEntity<Page<CandidateResponse>> getAll(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return ResponseEntity.ok(candidateService.getAll(pageable));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing candidate")
    public ResponseEntity<CandidateResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody CandidateRequest request) {
        return ResponseEntity.ok(candidateService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a candidate by ID")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        candidateService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    @Operation(summary = "Search candidates by keyword across name, email, company, and title")
    public ResponseEntity<Page<CandidateResponse>> search(
            @Parameter(description = "Search keyword")
            @RequestParam String keyword,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(candidateService.search(keyword, pageable));
    }

    @GetMapping("/filter")
    @Operation(summary = "Filter candidates using multiple criteria")
    public ResponseEntity<Page<CandidateResponse>> filter(
            @Parameter(description = "Free-text keyword")
            @RequestParam(required = false) String keyword,
            @Parameter(description = "Candidate pipeline status")
            @RequestParam(required = false) CandidateStatus status,
            @Parameter(description = "Minimum years of experience")
            @RequestParam(required = false) Integer minExperience,
            @Parameter(description = "Maximum years of experience")
            @RequestParam(required = false) Integer maxExperience,
            @Parameter(description = "Skill to filter by")
            @RequestParam(required = false) String skill,
            @Parameter(description = "Current company (partial match)")
            @RequestParam(required = false) String currentCompany,
            @PageableDefault(size = 20) Pageable pageable) {

        CandidateSearchCriteria criteria = CandidateSearchCriteria.builder()
                .keyword(keyword)
                .status(status)
                .minExperience(minExperience)
                .maxExperience(maxExperience)
                .skill(skill)
                .currentCompany(currentCompany)
                .build();

        return ResponseEntity.ok(candidateService.filter(criteria, pageable));
    }
}
