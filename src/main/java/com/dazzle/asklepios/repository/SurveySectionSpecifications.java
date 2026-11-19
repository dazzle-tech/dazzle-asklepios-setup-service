package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.SurveySection;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class SurveySectionSpecifications {

    private SurveySectionSpecifications() {}

    /** Every argument is optional; the ones given are combined with AND. Name matches English or Arabic. */
    public static Specification<SurveySection> search(Boolean isActive, String name) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (isActive != null) {
                predicates.add(cb.equal(root.get("isActive"), isActive));
            }
            if (name != null && !name.isBlank()) {
                String like = "%" + name.trim().toLowerCase(Locale.ROOT) + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("nameEn")), like),
                        cb.like(cb.lower(root.get("nameAr")), like)
                ));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
