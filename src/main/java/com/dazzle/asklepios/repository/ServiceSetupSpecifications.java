package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.ServiceSetup;
import com.dazzle.asklepios.domain.enumeration.Currency;
import com.dazzle.asklepios.domain.enumeration.ServiceCategory;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class ServiceSetupSpecifications {

    private ServiceSetupSpecifications() {}

    /** Every argument is optional; the ones given are combined with AND. */
    public static Specification<ServiceSetup> search(
            Boolean isActive,
            Long facilityId,
            ServiceCategory category,
            String name,
            String abbreviation,
            String code,
            BigDecimal price,
            Currency currency
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (isActive != null) {
                predicates.add(cb.equal(root.get("isActive"), isActive));
            }
            if (facilityId != null) {
                predicates.add(cb.equal(root.get("facility").get("id"), facilityId));
            }
            if (category != null) {
                predicates.add(cb.equal(root.get("category"), category));
            }
            if (hasText(name)) {
                predicates.add(cb.like(cb.lower(root.get("name")), toLike(name)));
            }
            if (hasText(abbreviation)) {
                predicates.add(cb.like(cb.lower(root.get("abbreviation")), toLike(abbreviation)));
            }
            if (hasText(code)) {
                predicates.add(cb.like(cb.lower(root.get("code")), toLike(code)));
            }
            if (price != null) {
                predicates.add(cb.equal(root.get("price"), price));
            }
            if (currency != null) {
                predicates.add(cb.equal(root.get("currency"), currency));
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
