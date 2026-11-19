package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.BrandMedication;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class BrandMedicationSpecifications {

    private BrandMedicationSpecifications() {}

    /** Every argument is optional; the ones given are combined with AND. Text filters are contains. */
    public static Specification<BrandMedication> search(
            Boolean isActive,
            String name,
            String manufacturer,
            String dosageForm,
            String usageInstructions,
            String roa,
            Boolean expiresAfterOpening,
            Boolean useSinglePatient
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (isActive != null) {
                predicates.add(cb.equal(root.get("isActive"), isActive));
            }
            addLike(predicates, cb, root.get("name"), name);
            addLike(predicates, cb, root.get("manufacturer"), manufacturer);
            addLike(predicates, cb, root.get("dosageForm"), dosageForm);
            addLike(predicates, cb, root.get("usageInstructions"), usageInstructions);
            addLike(predicates, cb, root.get("roa"), roa);
            if (expiresAfterOpening != null) {
                predicates.add(cb.equal(root.get("expiresAfterOpening"), expiresAfterOpening));
            }
            if (useSinglePatient != null) {
                predicates.add(cb.equal(root.get("useSinglePatient"), useSinglePatient));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private static void addLike(
            List<Predicate> predicates,
            jakarta.persistence.criteria.CriteriaBuilder cb,
            jakarta.persistence.criteria.Path<String> path,
            String value
    ) {
        if (value != null && !value.isBlank()) {
            predicates.add(cb.like(cb.lower(path), "%" + value.trim().toLowerCase(Locale.ROOT) + "%"));
        }
    }
}
