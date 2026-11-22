package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.BillingRule;
import com.dazzle.asklepios.domain.enumeration.biling.BillingItemTypes;
import com.dazzle.asklepios.domain.enumeration.biling.BillingRulePaymentType;
import com.dazzle.asklepios.domain.enumeration.biling.BillingTrigger;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class BillingRuleSpecifications {

    private BillingRuleSpecifications() {}

    /**
     * Every argument is optional; the ones given are combined with AND.
     * Created date is matched from createdFrom (inclusive) to createdTo (exclusive).
     */
    public static Specification<BillingRule> search(
            String name,
            BillingItemTypes billingItemType,
            BillingRulePaymentType paymentType,
            BillingTrigger billingTrigger,
            Boolean isDefault,
            Instant createdFrom,
            Instant createdTo
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (name != null && !name.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + name.trim().toLowerCase(Locale.ROOT) + "%"));
            }
            if (billingItemType != null) {
                predicates.add(cb.equal(root.get("billingItemType"), billingItemType));
            }
            if (paymentType != null) {
                predicates.add(cb.equal(root.get("paymentType"), paymentType));
            }
            if (billingTrigger != null) {
                predicates.add(cb.equal(root.get("billingTrigger"), billingTrigger));
            }
            if (isDefault != null) {
                predicates.add(cb.equal(root.get("isDefault"), isDefault));
            }
            if (createdFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdDate"), createdFrom));
            }
            if (createdTo != null) {
                predicates.add(cb.lessThan(root.get("createdDate"), createdTo));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
