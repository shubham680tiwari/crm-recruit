package com.recruitcrm.candidatemanager.dto;

import com.recruitcrm.candidatemanager.entity.Candidate;
import com.recruitcrm.candidatemanager.entity.CandidateStatus;
import org.mapstruct.*;

/**
 * MapStruct mapper for converting between Candidate entity and DTOs.
 * Generates type-safe mapping code at compile time — no reflection at runtime.
 */
@Mapper(componentModel = "spring")
public interface CandidateMapper {

    CandidateResponse toResponse(Candidate candidate);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Candidate toEntity(CandidateRequest request);

    /**
     * Merges non-null fields from the request into an existing entity.
     * Null fields in the request are left unchanged on the entity.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(CandidateRequest request, @MappingTarget Candidate candidate);

    /**
     * Sets a default status of NEW when the request status is null (for new candidates).
     */
    default CandidateStatus mapStatus(CandidateStatus status) {
        return status != null ? status : CandidateStatus.NEW;
    }
}
