package com.recruitcrm.candidatemanager.dto;

import lombok.*;

import java.util.Map;

/**
 * Standardized error response body returned for all API errors.
 * Provides a consistent structure for clients to parse.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponse {
    private int status;
    private String error;
    private String message;

    /** Field-level validation errors: field name → error message. */
    private Map<String, String> details;
}
