package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.FormTemplate;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class FormTemplateSpecifications {

    private FormTemplateSpecifications() {}

    /** Every argument is optional; the ones given are combined with AND (text = contains, case ignored). */
    public static Specification<FormTemplate> search(Long facilityId, Long departmentId, String name, String description) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (facilityId != null) {
                predicates.add(cb.equal(root.get("facility").get("id"), facilityId));
            }
            if (departmentId != null) {
                predicates.add(cb.equal(root.get("department").get("id"), departmentId));
            }
            if (name != null && !name.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + name.trim().toLowerCase(Locale.ROOT) + "%"));
            }
            if (description != null && !description.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("description")), "%" + description.trim().toLowerCase(Locale.ROOT) + "%"));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
