package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.DentalChartAgeRule;
import com.dazzle.asklepios.domain.enumeration.DentalChartType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class DentalChartAgeRuleSpecifications {

    private DentalChartAgeRuleSpecifications() {}

    /** Every argument is optional; the ones given are combined with AND. */
    public static Specification<DentalChartAgeRule> search(Boolean isActive, String name, DentalChartType chartType) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (isActive != null) {
                predicates.add(cb.equal(root.get("isActive"), isActive));
            }
            if (name != null && !name.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + name.trim().toLowerCase(Locale.ROOT) + "%"));
            }
            if (chartType != null) {
                predicates.add(cb.equal(root.get("chartType"), chartType));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
