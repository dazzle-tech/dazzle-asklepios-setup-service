package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.Discount;
import com.dazzle.asklepios.domain.enumeration.DiscountApplicableOn;
import com.dazzle.asklepios.domain.enumeration.DiscountType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class DiscountSpecifications {

    private DiscountSpecifications() {}

    /**
     * Discounts of one facility; every other argument is optional and the ones given are combined with AND.
     * Value matches the percentage or the fixed amount (same as the by-value search).
     */
    public static Specification<Discount> search(
            Long facilityId,
            String code,
            String name,
            DiscountType discountType,
            BigDecimal value,
            DiscountApplicableOn applicableOn,
            Boolean requiresApproval,
            Boolean isDefault,
            Boolean active
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("facilityId"), facilityId));
            if (code != null && !code.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("code")), "%" + code.trim().toLowerCase(Locale.ROOT) + "%"));
            }
            if (name != null && !name.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + name.trim().toLowerCase(Locale.ROOT) + "%"));
            }
            if (discountType != null) {
                predicates.add(cb.equal(root.get("discountType"), discountType));
            }
            if (value != null) {
                predicates.add(cb.or(
                        cb.equal(root.get("percentage"), value),
                        cb.equal(root.get("fixedAmount"), value)
                ));
            }
            if (applicableOn != null) {
                predicates.add(cb.equal(root.get("applicableOn"), applicableOn));
            }
            if (requiresApproval != null) {
                predicates.add(cb.equal(root.get("requiresApproval"), requiresApproval));
            }
            if (isDefault != null) {
                predicates.add(cb.equal(root.get("isDefault"), isDefault));
            }
            if (active != null) {
                predicates.add(cb.equal(root.get("active"), active));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
