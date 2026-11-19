package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.VisitDuration;
import com.dazzle.asklepios.domain.enumeration.VisitType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class VisitDurationSpecifications {

    private VisitDurationSpecifications() {}

    /** Every argument is optional; the ones given are combined with AND. */
    public static Specification<VisitDuration> search(
            VisitType visitType,
            Integer durationInMinutes,
            Boolean resourceSpecific
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (visitType != null) {
                predicates.add(cb.equal(root.get("visitType"), visitType));
            }
            if (durationInMinutes != null) {
                predicates.add(cb.equal(root.get("durationInMinutes"), durationInMinutes));
            }
            if (resourceSpecific != null) {
                predicates.add(cb.equal(root.get("resourceSpecific"), resourceSpecific));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
