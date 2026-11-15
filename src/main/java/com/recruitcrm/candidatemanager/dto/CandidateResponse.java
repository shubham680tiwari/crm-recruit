package com.recruitcrm.candidatemanager.dto;

import com.recruitcrm.candidatemanager.entity.CandidateStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Outbound DTO returned to API consumers.
 * Decouples the internal entity schema from the public contract.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CandidateResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String currentCompany;
    private String currentTitle;
    private CandidateStatus status;
    private Integer yearsOfExperience;
    private String expectedSalary;
    private String notes;
    private List<String> skills;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
