package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.ReportTemplate;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class ReportTemplateSpecifications {

    private ReportTemplateSpecifications() {}

    /** Every argument is optional; the ones given are combined with AND. */
    public static Specification<ReportTemplate> search(Boolean isActive, String name) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (isActive != null) {
                predicates.add(cb.equal(root.get("isActive"), isActive));
            }
            if (name != null && !name.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + name.trim().toLowerCase(Locale.ROOT) + "%"));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
