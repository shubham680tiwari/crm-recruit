package com.recruitcrm.candidatemanager.exception;

/**
 * Thrown when a candidate email conflicts with an existing record.
 */
public class DuplicateEmailException extends RuntimeException {

    public DuplicateEmailException(String email) {
        super("A candidate with email '" + email + "' already exists");
    }
}
