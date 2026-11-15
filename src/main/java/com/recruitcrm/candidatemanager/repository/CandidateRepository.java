package com.recruitcrm.candidatemanager.repository;

import com.recruitcrm.candidatemanager.entity.Candidate;
import com.recruitcrm.candidatemanager.entity.CandidateStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for Candidate entities.
 * Extends JpaSpecificationExecutor to support dynamic, multi-criteria filtering.
 */
@Repository
public interface CandidateRepository extends JpaRepository<Candidate, Long>,
        JpaSpecificationExecutor<Candidate> {

    Optional<Candidate> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    Page<Candidate> findByStatus(CandidateStatus status, Pageable pageable);

    /**
     * Full-text keyword search across name, email, company, and title.
     * For production scale, this would be replaced with Elasticsearch or a full-text index.
     */
    @Query("""
        SELECT c FROM Candidate c
        WHERE LOWER(c.firstName) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(c.lastName)  LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(c.email)     LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(c.currentCompany) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(c.currentTitle)   LIKE LOWER(CONCAT('%', :keyword, '%'))
    """)
    Page<Candidate> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
}
