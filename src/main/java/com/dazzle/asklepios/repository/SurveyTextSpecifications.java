package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.SurveyText;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class SurveyTextSpecifications {

    private SurveyTextSpecifications() {}

    /** Every argument is optional; the ones given are combined with AND. Text matches the key, English or Arabic. */
    public static Specification<SurveyText> search(Boolean isActive, String text) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (isActive != null) {
                predicates.add(cb.equal(root.get("isActive"), isActive));
            }
            if (text != null && !text.isBlank()) {
                String like = "%" + text.trim().toLowerCase(Locale.ROOT) + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("textKey")), like),
                        cb.like(cb.lower(root.get("textEn")), like),
                        cb.like(cb.lower(root.get("textAr")), like)
                ));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
