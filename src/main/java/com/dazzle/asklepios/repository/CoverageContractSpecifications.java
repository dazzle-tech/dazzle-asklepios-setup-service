package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.CoverageContract;
import com.dazzle.asklepios.domain.enumeration.GuarantorType;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class CoverageContractSpecifications {

    private CoverageContractSpecifications() {}

    public static Specification<CoverageContract> search(
            GuarantorType guarantorType,
            Long companyId,
            Long insurancePayerId,
            Boolean isActive,
            String search
    ) {
        return (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
            if (guarantorType != null) {
                predicates.add(cb.equal(root.get("guarantorType"), guarantorType));
            }
            if (companyId != null) {
                predicates.add(cb.equal(root.get("companyId"), companyId));
            }
            if (insurancePayerId != null) {
                predicates.add(cb.equal(root.get("insurancePayerId"), insurancePayerId));
            }
            if (isActive != null) {
                predicates.add(cb.equal(root.get("isActive"), isActive));
            }
            if (search != null && !search.isBlank()) {
                String like = "%" + search.trim().toLowerCase(Locale.ROOT) + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("code")), like),
                        cb.like(cb.lower(root.get("policyNumber")), like)
                ));
            }
            return cb.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
    }
}
