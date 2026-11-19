package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.DiagnosticTest;
import com.dazzle.asklepios.domain.enumeration.TestType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class DiagnosticTestSpecifications {

    private DiagnosticTestSpecifications() {}

    /** Every argument is optional; the ones given are combined with AND. */
    public static Specification<DiagnosticTest> search(
            Boolean isActive,
            TestType type,
            String name,
            String shortName,
            String internalCode
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (isActive != null) {
                predicates.add(cb.equal(root.get("isActive"), isActive));
            }
            if (type != null) {
                predicates.add(cb.equal(root.get("type"), type));
            }
            if (hasText(name)) {
                predicates.add(cb.like(cb.lower(root.get("name")), toLike(name)));
            }
            if (hasText(shortName)) {
                predicates.add(cb.like(cb.lower(root.get("shortName")), toLike(shortName)));
            }
            if (hasText(internalCode)) {
                predicates.add(cb.like(cb.lower(root.get("internalCode")), toLike(internalCode)));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private static String toLike(String value) {
        return "%" + value.trim().toLowerCase(Locale.ROOT) + "%";
    }
}
