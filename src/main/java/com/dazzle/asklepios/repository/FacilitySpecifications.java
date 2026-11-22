package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.Facility;
import com.dazzle.asklepios.domain.enumeration.FacilityType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class FacilitySpecifications {

    private FacilitySpecifications() {}

    /** Every argument is optional; the ones given are combined with AND (text = contains, case ignored). */
    public static Specification<Facility> search(
            String code,
            String name,
            FacilityType type,
            String emailAddress,
            Boolean isActive
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (code != null && !code.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("code")), "%" + code.trim().toLowerCase(Locale.ROOT) + "%"));
            }
            if (name != null && !name.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + name.trim().toLowerCase(Locale.ROOT) + "%"));
            }
            if (type != null) {
                predicates.add(cb.equal(root.get("type"), type));
            }
            if (emailAddress != null && !emailAddress.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("emailAddress")), "%" + emailAddress.trim().toLowerCase(Locale.ROOT) + "%"));
            }
            if (isActive != null) {
                predicates.add(cb.equal(root.get("isActive"), isActive));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
