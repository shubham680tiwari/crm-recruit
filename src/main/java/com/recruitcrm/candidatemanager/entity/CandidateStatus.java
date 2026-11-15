package com.recruitcrm.candidatemanager.entity;

/**
 * Tracks a candidate's position in the recruitment pipeline.
 */
public enum CandidateStatus {
    NEW,
    CONTACTED,
    INTERVIEWING,
    OFFERED,
    HIRED,
    REJECTED,
    WITHDRAWN
}
