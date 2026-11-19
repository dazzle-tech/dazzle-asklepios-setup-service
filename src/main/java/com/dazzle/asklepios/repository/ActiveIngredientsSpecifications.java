package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.ActiveIngredients;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class ActiveIngredientsSpecifications {

    private ActiveIngredientsSpecifications() {}

    /** Every argument is optional; the ones given are combined with AND. */
    public static Specification<ActiveIngredients> search(
            Boolean isActive,
            String name,
            String atcCode,
            List<Long> drugClassIds
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (isActive != null) {
                predicates.add(cb.equal(root.get("isActive"), isActive));
            }
            if (name != null && !name.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("name")), toLike(name)));
            }
            if (atcCode != null && !atcCode.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("atcCode")), toLike(atcCode)));
            }
            if (drugClassIds != null && !drugClassIds.isEmpty()) {
                predicates.add(root.get("drugClass").get("id").in(drugClassIds));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private static String toLike(String value) {
        return "%" + value.trim().toLowerCase(Locale.ROOT) + "%";
    }
}
