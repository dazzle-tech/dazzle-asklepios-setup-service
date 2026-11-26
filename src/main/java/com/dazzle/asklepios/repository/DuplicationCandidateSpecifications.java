package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.DuplicationCandidate;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class DuplicationCandidateSpecifications {

    private DuplicationCandidateSpecifications() {}

    /** Every argument is optional; the ones given are combined with AND. */
    public static Specification<DuplicationCandidate> search(String rule, Boolean isActive) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (rule != null && !rule.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("rule")), "%" + rule.trim().toLowerCase(Locale.ROOT) + "%"));
            }
            if (isActive != null) {
                predicates.add(cb.equal(root.get("isActive"), isActive));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
