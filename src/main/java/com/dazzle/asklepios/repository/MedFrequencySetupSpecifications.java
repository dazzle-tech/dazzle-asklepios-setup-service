package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.MedFrequencySetup;
import com.dazzle.asklepios.domain.enumeration.FrequencyList;
import com.dazzle.asklepios.domain.enumeration.MedFrequency;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class MedFrequencySetupSpecifications {

    private MedFrequencySetupSpecifications() {}

    /** Every argument is optional; the ones given are combined with AND. */
    public static Specification<MedFrequencySetup> search(
            Boolean isActive,
            MedFrequency frequency,
            Integer frequencyNumber,
            FrequencyList frequencyUnit
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (isActive != null) {
                predicates.add(cb.equal(root.get("isActive"), isActive));
            }
            if (frequency != null) {
                predicates.add(cb.equal(root.get("frequency"), frequency));
            }
            if (frequencyNumber != null) {
                predicates.add(cb.equal(root.get("frequencyNumber"), frequencyNumber));
            }
            if (frequencyUnit != null) {
                predicates.add(cb.equal(root.get("frequencyUnit"), frequencyUnit));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
