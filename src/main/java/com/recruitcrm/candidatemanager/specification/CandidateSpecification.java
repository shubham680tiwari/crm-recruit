package com.recruitcrm.candidatemanager.specification;

import com.recruitcrm.candidatemanager.dto.CandidateSearchCriteria;
import com.recruitcrm.candidatemanager.entity.Candidate;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds a dynamic JPA Specification from search criteria.
 * Each non-null filter field adds a predicate; all predicates are ANDed together.
 * This approach keeps query logic composable and avoids hand-written JPQL per combination.
 */
public final class CandidateSpecification {

    private CandidateSpecification() {
        // Utility class — not instantiable
    }

    public static Specification<Candidate> fromCriteria(CandidateSearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Free-text keyword: OR across multiple columns
            if (criteria.getKeyword() != null && !criteria.getKeyword().isBlank()) {
                String pattern = "%" + criteria.getKeyword().toLowerCase() + "%";
                predicates.add(cb.or(
                    cb.like(cb.lower(root.get("firstName")), pattern),
                    cb.like(cb.lower(root.get("lastName")), pattern),
                    cb.like(cb.lower(root.get("email")), pattern),
                    cb.like(cb.lower(root.get("currentCompany")), pattern),
                    cb.like(cb.lower(root.get("currentTitle")), pattern)
                ));
            }

            // Status filter
            if (criteria.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), criteria.getStatus()));
            }

            // Experience range filters
            if (criteria.getMinExperience() != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                    root.get("yearsOfExperience"), criteria.getMinExperience()));
            }
            if (criteria.getMaxExperience() != null) {
                predicates.add(cb.lessThanOrEqualTo(
                    root.get("yearsOfExperience"), criteria.getMaxExperience()));
            }

            // Skill filter: join the skills collection and match case-insensitively
            if (criteria.getSkill() != null && !criteria.getSkill().isBlank()) {
                Join<Candidate, String> skillJoin = root.join("skills", JoinType.INNER);
                predicates.add(cb.like(cb.lower(skillJoin), "%" + criteria.getSkill().toLowerCase() + "%"));

                // Prevent duplicate results from the join
                query.distinct(true);
            }

            // Company filter
            if (criteria.getCurrentCompany() != null && !criteria.getCurrentCompany().isBlank()) {
                predicates.add(cb.like(
                    cb.lower(root.get("currentCompany")),
                    "%" + criteria.getCurrentCompany().toLowerCase() + "%"));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
