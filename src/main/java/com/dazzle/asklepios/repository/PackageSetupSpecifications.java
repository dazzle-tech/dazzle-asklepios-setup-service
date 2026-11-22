package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.PackageSetup;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public final class PackageSetupSpecifications {

    private PackageSetupSpecifications() {}

    /** Every argument is optional; the ones given are combined with AND. */
    public static Specification<PackageSetup> search(String code, String name, BigDecimal price, Boolean isActive) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (code != null && !code.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("code")), "%" + code.trim().toLowerCase() + "%"));
            }
            if (name != null && !name.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + name.trim().toLowerCase() + "%"));
            }
            if (price != null) {
                predicates.add(cb.equal(root.get("price"), price));
            }
            if (isActive != null) {
                predicates.add(cb.equal(root.get("isActive"), isActive));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
