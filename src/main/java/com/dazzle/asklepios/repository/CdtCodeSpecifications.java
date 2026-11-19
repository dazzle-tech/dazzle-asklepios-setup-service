package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.CdtCode;
import com.dazzle.asklepios.domain.enumeration.CdtClass;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class CdtCodeSpecifications {

    private CdtCodeSpecifications() {}

    /** Every argument is optional; the ones given are combined with AND. */
    public static Specification<CdtCode> search(
            Boolean isActive,
            CdtClass cdtClass,
            String code,
            String description
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (isActive != null) {
                predicates.add(cb.equal(root.get("isActive"), isActive));
            }
            if (cdtClass != null) {
                predicates.add(cb.equal(root.get("cdtClass"), cdtClass));
            }
            if (hasText(code)) {
                predicates.add(cb.like(cb.lower(root.get("code")), toLike(code)));
            }
            if (hasText(description)) {
                predicates.add(cb.like(cb.lower(root.get("description")), toLike(description)));
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
