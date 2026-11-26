package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.Catalog;
import com.dazzle.asklepios.domain.enumeration.TestType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class CatalogSpecifications {

    private CatalogSpecifications() {}

    /** Every argument is optional; the ones given are combined with AND. */
    public static Specification<Catalog> search(String name, TestType type, Long departmentId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (name != null && !name.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + name.trim().toLowerCase(Locale.ROOT) + "%"));
            }
            if (type != null) {
                predicates.add(cb.equal(root.get("type"), type));
            }
            if (departmentId != null) {
                predicates.add(cb.equal(root.get("department").get("id"), departmentId));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
