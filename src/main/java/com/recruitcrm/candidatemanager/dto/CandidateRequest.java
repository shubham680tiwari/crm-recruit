package com.recruitcrm.candidatemanager.dto;

import com.recruitcrm.candidatemanager.entity.CandidateStatus;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

/**
 * Inbound DTO for creating or updating a candidate.
 * Validation constraints enforce data integrity at the API boundary.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CandidateRequest {

    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name must not exceed 100 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name must not exceed 100 characters")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;

    @Pattern(regexp = "^\\+?[0-9\\-\\s]{7,20}$", message = "Phone number format is invalid")
    private String phone;

    @Size(max = 200, message = "Company name must not exceed 200 characters")
    private String currentCompany;

    @Size(max = 150, message = "Title must not exceed 150 characters")
    private String currentTitle;

    private CandidateStatus status;

    @Min(value = 0, message = "Years of experience cannot be negative")
    @Max(value = 60, message = "Years of experience seems unrealistic")
    private Integer yearsOfExperience;

    @Size(max = 500, message = "Expected salary must not exceed 500 characters")
    private String expectedSalary;

    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;

    private List<@Size(max = 100, message = "Each skill must not exceed 100 characters") String> skills;
}
