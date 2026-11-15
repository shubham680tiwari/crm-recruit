package com.recruitcrm.candidatemanager.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Core domain entity representing a recruitment candidate.
 * Uses optimistic locking (@Version) to handle concurrent updates safely.
 */
@Entity
@Table(
    name = "candidates",
    indexes = {
        @Index(name = "idx_candidate_email", columnList = "email", unique = true),
        @Index(name = "idx_candidate_status", columnList = "status"),
        @Index(name = "idx_candidate_name", columnList = "firstName, lastName")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Candidate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String firstName;

    @Column(nullable = false, length = 100)
    private String lastName;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(length = 200)
    private String currentCompany;

    @Column(length = 150)
    private String currentTitle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private CandidateStatus status = CandidateStatus.NEW;

    @Column(name = "years_of_experience")
    private Integer yearsOfExperience;

    @Column(length = 500)
    private String expectedSalary;

    @Column(length = 1000)
    private String notes;

    /**
     * Stores skills as a comma-separated list in a single column.
     * For high-scale scenarios, this would be normalized into a separate table.
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "candidate_skills", joinColumns = @JoinColumn(name = "candidate_id"))
    @Column(name = "skill", length = 100)
    @Builder.Default
    private List<String> skills = new ArrayList<>();

    @Version
    private Long version;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
