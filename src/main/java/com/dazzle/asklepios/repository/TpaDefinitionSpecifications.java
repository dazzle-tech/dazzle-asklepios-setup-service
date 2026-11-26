package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.TpaDefinition;
import com.dazzle.asklepios.domain.enumeration.GuarantorType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class TpaDefinitionSpecifications {

    private TpaDefinitionSpecifications() {}

    /** Every argument is optional; the ones given are combined with AND (text = contains, case ignored). */
    public static Specification<TpaDefinition> search(
            String tpaCode,
            String name,
            GuarantorType guarantorType,
            Boolean isActive
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (tpaCode != null && !tpaCode.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("tpaCode")), "%" + tpaCode.trim().toLowerCase(Locale.ROOT) + "%"));
            }
            if (name != null && !name.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + name.trim().toLowerCase(Locale.ROOT) + "%"));
            }
            if (guarantorType != null) {
                predicates.add(cb.equal(root.get("guarantorType"), guarantorType));
            }
            if (isActive != null) {
                predicates.add(cb.equal(root.get("isActive"), isActive));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
