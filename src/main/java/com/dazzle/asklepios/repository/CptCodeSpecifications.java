package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.CptCode;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class CptCodeSpecifications {

    private CptCodeSpecifications() {}

    /**
     * Every argument is optional; the ones given are combined with AND.
     * All text filters are case-insensitive contains (the screen types them as free text).
     */
    public static Specification<CptCode> search(
            String mainCategory,
            String serviceCategory,
            String code,
            String description
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (hasText(mainCategory)) {
                predicates.add(cb.like(cb.lower(root.get("mainCategory")), "%" + lower(mainCategory) + "%"));
            }
            if (hasText(serviceCategory)) {
                predicates.add(cb.like(cb.lower(root.get("serviceCategory")), "%" + lower(serviceCategory) + "%"));
            }
            if (hasText(code)) {
                predicates.add(cb.like(cb.lower(root.get("code")), "%" + lower(code) + "%"));
            }
            if (hasText(description)) {
                predicates.add(cb.like(cb.lower(root.get("description")), "%" + lower(description) + "%"));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private static String lower(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
