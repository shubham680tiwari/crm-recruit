package com.recruitcrm.candidatemanager.dto;

import com.recruitcrm.candidatemanager.entity.CandidateStatus;
import lombok.*;

/**
 * Encapsulates multi-criteria search/filter parameters.
 * All fields are optional — only non-null values are applied as filters.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CandidateSearchCriteria {

    /** Free-text keyword; matched against name, email, company, title, and skills. */
    private String keyword;

    private CandidateStatus status;

    private Integer minExperience;
    private Integer maxExperience;

    /** Exact skill match (case-insensitive). */
    private String skill;

    private String currentCompany;
}
