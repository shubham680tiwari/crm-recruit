package com.recruitcrm.candidatemanager.exception;

/**
 * Thrown when a requested candidate does not exist.
 */
public class CandidateNotFoundException extends RuntimeException {

    public CandidateNotFoundException(Long id) {
        super("Candidate not found with id: " + id);
    }
}
