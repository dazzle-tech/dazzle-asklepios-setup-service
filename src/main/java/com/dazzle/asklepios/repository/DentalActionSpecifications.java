package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.DentalAction;
import com.dazzle.asklepios.domain.enumeration.DentalActionType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class DentalActionSpecifications {

    private DentalActionSpecifications() {}

    /** Every argument is optional; the ones given are combined with AND. */
    public static Specification<DentalAction> search(Boolean isActive, DentalActionType type, String description) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (isActive != null) {
                predicates.add(cb.equal(root.get("isActive"), isActive));
            }
            if (type != null) {
                predicates.add(cb.equal(root.get("type"), type));
            }
            if (description != null && !description.isBlank()) {
                predicates.add(cb.like(
                        cb.lower(root.get("description")),
                        "%" + description.trim().toLowerCase(Locale.ROOT) + "%"
                ));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
