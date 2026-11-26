package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.LoincCode;
import com.dazzle.asklepios.domain.enumeration.LoincCategory;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class LoincCodeSpecifications {

    private LoincCodeSpecifications() {}

    /** Every argument is optional; the ones given are combined with AND. */
    public static Specification<LoincCode> search(LoincCategory category, String code, String description) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (category != null) {
                predicates.add(cb.equal(root.get("category"), category));
            }
            if (code != null && !code.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("code")), toLike(code)));
            }
            if (description != null && !description.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("description")), toLike(description)));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private static String toLike(String value) {
        return "%" + value.trim().toLowerCase(Locale.ROOT) + "%";
    }
}
