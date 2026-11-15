package com.recruitcrm.candidatemanager.service;

import com.recruitcrm.candidatemanager.dto.*;
import com.recruitcrm.candidatemanager.entity.Candidate;
import com.recruitcrm.candidatemanager.exception.CandidateNotFoundException;
import com.recruitcrm.candidatemanager.exception.DuplicateEmailException;
import com.recruitcrm.candidatemanager.repository.CandidateRepository;
import com.recruitcrm.candidatemanager.specification.CandidateSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Business logic layer for candidate operations.
 * All public methods are transactional; read-only where possible for performance.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CandidateService {

    private final CandidateRepository repository;
    private final CandidateMapper mapper;

    @Transactional
    public CandidateResponse create(CandidateRequest request) {
        // Guard against duplicate emails
        if (repository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new DuplicateEmailException(request.getEmail());
        }

        Candidate entity = mapper.toEntity(request);
        Candidate saved = repository.save(entity);
        log.info("Created candidate id={} email={}", saved.getId(), saved.getEmail());
        return mapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public CandidateResponse getById(Long id) {
        Candidate candidate = findOrThrow(id);
        return mapper.toResponse(candidate);
    }

    @Transactional(readOnly = true)
    public Page<CandidateResponse> getAll(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toResponse);
    }

    @Transactional
    public CandidateResponse update(Long id, CandidateRequest request) {
        Candidate existing = findOrThrow(id);

        // If the email is changing, ensure the new one is not already taken
        if (request.getEmail() != null
                && !existing.getEmail().equalsIgnoreCase(request.getEmail())
                && repository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new DuplicateEmailException(request.getEmail());
        }

        mapper.updateEntityFromRequest(request, existing);
        Candidate saved = repository.save(existing);
        log.info("Updated candidate id={}", saved.getId());
        return mapper.toResponse(saved);
    }

    @Transactional
    public void delete(Long id) {
        Candidate candidate = findOrThrow(id);
        repository.delete(candidate);
        log.info("Deleted candidate id={}", id);
    }

    /**
     * Keyword-based search across name, email, company, and title.
     */
    @Transactional(readOnly = true)
    public Page<CandidateResponse> search(String keyword, Pageable pageable) {
        return repository.searchByKeyword(keyword, pageable).map(mapper::toResponse);
    }

    /**
     * Advanced multi-criteria filter using JPA Specifications.
     * Supports combining status, experience range, skill, company, and keyword filters.
     */
    @Transactional(readOnly = true)
    public Page<CandidateResponse> filter(CandidateSearchCriteria criteria, Pageable pageable) {
        return repository.findAll(CandidateSpecification.fromCriteria(criteria), pageable)
                .map(mapper::toResponse);
    }

    private Candidate findOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new CandidateNotFoundException(id));
    }
}
