package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.Tax;
import com.dazzle.asklepios.domain.enumeration.TaxApplicableOn;
import com.dazzle.asklepios.domain.enumeration.TaxCalculationType;
import com.dazzle.asklepios.domain.enumeration.TaxCitizenCategory;
import com.dazzle.asklepios.domain.enumeration.TaxType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class TaxSpecifications {

    private TaxSpecifications() {}

    /**
     * Taxes of one facility; every other argument is optional and the ones given are combined with AND.
     * Value matches the percentage or the fixed amount (same as the by-value search).
     */
    public static Specification<Tax> search(
            Long facilityId,
            String code,
            String name,
            TaxType taxType,
            BigDecimal value,
            TaxCalculationType calculationType,
            TaxApplicableOn applicableOn,
            TaxCitizenCategory citizenCategory,
            Boolean applyOnPatientCopayment,
            Boolean applyOnClaimShare,
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
            if (taxType != null) {
                predicates.add(cb.equal(root.get("taxType"), taxType));
            }
            if (value != null) {
                predicates.add(cb.or(
                        cb.equal(root.get("percentage"), value),
                        cb.equal(root.get("fixedAmount"), value)
                ));
            }
            if (calculationType != null) {
                predicates.add(cb.equal(root.get("calculationType"), calculationType));
            }
            if (applicableOn != null) {
                predicates.add(cb.equal(root.get("applicableOn"), applicableOn));
            }
            if (citizenCategory != null) {
                predicates.add(cb.equal(root.get("citizenCategory"), citizenCategory));
            }
            if (applyOnPatientCopayment != null) {
                predicates.add(cb.equal(root.get("applyOnPatientCopayment"), applyOnPatientCopayment));
            }
            if (applyOnClaimShare != null) {
                predicates.add(cb.equal(root.get("applyOnClaimShare"), applyOnClaimShare));
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
