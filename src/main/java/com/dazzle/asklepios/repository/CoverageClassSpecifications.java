package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.CoverageClass;
import com.dazzle.asklepios.domain.InsuranceCompanyPolicy;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class CoverageClassSpecifications {

    private CoverageClassSpecifications() {}

    public static Specification<CoverageClass> search(Long contractId, Boolean isActive, String name, String policy) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("coverageContract").get("id"), contractId));
            if (isActive != null) {
                predicates.add(cb.equal(root.get("isActive"), isActive));
            }
            if (hasText(name)) {
                predicates.add(cb.like(cb.lower(root.get("name")), toLike(name)));
            }
            if (hasText(policy)) {
                String like = toLike(policy);
                Join<CoverageClass, InsuranceCompanyPolicy> policyJoin = root.join("insuranceCompanyPolicy", JoinType.LEFT);
                predicates.add(cb.or(
                        cb.like(cb.lower(policyJoin.get("policyName")), like),
                        cb.like(cb.lower(policyJoin.get("policyNumber")), like),
                        cb.like(cb.lower(policyJoin.get("policyCode")), like)
                ));
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
