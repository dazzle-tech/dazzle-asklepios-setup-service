package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.Vaccine;
import com.dazzle.asklepios.domain.enumeration.RouteOfAdministration;
import com.dazzle.asklepios.domain.enumeration.VaccineType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class VaccineSpecifications {

    private VaccineSpecifications() {}

    /** Every argument is optional; the ones given are combined with AND. */
    public static Specification<Vaccine> search(
            Boolean isActive,
            String name,
            VaccineType type,
            RouteOfAdministration roa
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (isActive != null) {
                predicates.add(cb.equal(root.get("isActive"), isActive));
            }
            if (hasText(name)) {
                predicates.add(cb.like(cb.lower(root.get("name")), toLike(name)));
            }
            if (type != null) {
                predicates.add(cb.equal(root.get("type"), type));
            }
            if (roa != null) {
                predicates.add(cb.equal(root.get("roa"), roa));
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
