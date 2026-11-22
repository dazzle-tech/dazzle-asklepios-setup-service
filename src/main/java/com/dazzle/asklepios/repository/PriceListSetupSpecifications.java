package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.PriceListSetup;
import com.dazzle.asklepios.domain.enumeration.Currency;
import com.dazzle.asklepios.domain.enumeration.PriceListSetupStatus;
import com.dazzle.asklepios.domain.enumeration.PriceListSetupType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class PriceListSetupSpecifications {

    private PriceListSetupSpecifications() {}

    /**
     * Every argument is optional; the ones given are combined with AND.
     * Each one matches like its by-... search (name contains, the rest exact).
     */
    public static Specification<PriceListSetup> search(
            Long facilityId,
            String name,
            PriceListSetupType type,
            Long payerId,
            Integer versionNumber,
            LocalDate effectiveFrom,
            LocalDate effectiveTo,
            Currency currency,
            PriceListSetupStatus status
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (facilityId != null) {
                predicates.add(cb.equal(root.get("facilityId"), facilityId));
            }
            if (name != null && !name.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + name.trim().toLowerCase(Locale.ROOT) + "%"));
            }
            if (type != null) {
                predicates.add(cb.equal(root.get("type"), type));
            }
            if (payerId != null) {
                predicates.add(cb.equal(root.get("payerId"), payerId));
            }
            if (versionNumber != null) {
                predicates.add(cb.equal(root.get("versionNumber"), versionNumber));
            }
            if (effectiveFrom != null) {
                predicates.add(cb.equal(root.get("effectiveFrom"), effectiveFrom));
            }
            if (effectiveTo != null) {
                predicates.add(cb.equal(root.get("effectiveTo"), effectiveTo));
            }
            if (currency != null) {
                predicates.add(cb.equal(root.get("currency"), currency));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
