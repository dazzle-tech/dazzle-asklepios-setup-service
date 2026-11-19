package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.AgeGroup;
import com.dazzle.asklepios.domain.enumeration.AgeGroupType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public final class AgeGroupSpecifications {

    private AgeGroupSpecifications() {}

    /** Every argument is optional; the ones given are combined with AND. Ages are exact matches. */
    public static Specification<AgeGroup> search(
            Long facilityId,
            AgeGroupType ageGroup,
            BigDecimal fromAge,
            BigDecimal toAge
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (facilityId != null) {
                predicates.add(cb.equal(root.get("facility").get("id"), facilityId));
            }
            if (ageGroup != null) {
                predicates.add(cb.equal(root.get("ageGroup"), ageGroup));
            }
            if (fromAge != null) {
                predicates.add(cb.equal(root.get("fromAge"), fromAge));
            }
            if (toAge != null) {
                predicates.add(cb.equal(root.get("toAge"), toAge));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
