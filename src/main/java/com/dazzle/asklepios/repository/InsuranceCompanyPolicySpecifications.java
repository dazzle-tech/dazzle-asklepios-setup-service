package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.InsuranceCompanyPolicy;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class InsuranceCompanyPolicySpecifications {

    private InsuranceCompanyPolicySpecifications() {}

    public static Specification<InsuranceCompanyPolicy> search(
            Long insuranceCompanyId,
            String policyName,
            String policyNumber,
            String policyCode
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("insuranceCompany").get("id"), insuranceCompanyId));
            if (hasText(policyName)) {
                predicates.add(cb.like(cb.lower(root.get("policyName")), toLike(policyName)));
            }
            if (hasText(policyNumber)) {
                predicates.add(cb.like(cb.lower(root.get("policyNumber")), toLike(policyNumber)));
            }
            if (hasText(policyCode)) {
                predicates.add(cb.like(cb.lower(root.get("policyCode")), toLike(policyCode)));
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
