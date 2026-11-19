package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.PolicyDefinition;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class PolicyDefinitionSpecifications {

    private PolicyDefinitionSpecifications() {}

    /** Every argument is optional; the ones given are combined with AND. */
    public static Specification<PolicyDefinition> search(Boolean isActive, Long facilityId, String code, String name) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (isActive != null) {
                predicates.add(cb.equal(root.get("isActive"), isActive));
            }
            if (facilityId != null) {
                predicates.add(cb.equal(root.get("facility").get("id"), facilityId));
            }
            if (hasText(code)) {
                predicates.add(cb.like(cb.lower(root.get("code")), toLike(code)));
            }
            if (hasText(name)) {
                predicates.add(cb.like(cb.lower(root.get("name")), toLike(name)));
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
