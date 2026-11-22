package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.PrescriptionInstruction;
import com.dazzle.asklepios.domain.enumeration.AgeGroupType;
import com.dazzle.asklepios.domain.enumeration.MedFrequency;
import com.dazzle.asklepios.domain.enumeration.MedRoa;
import com.dazzle.asklepios.domain.enumeration.UOM;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public final class PrescriptionInstructionSpecifications {

    private PrescriptionInstructionSpecifications() {}

    /** Every argument is optional; the ones given are combined with AND. */
    public static Specification<PrescriptionInstruction> search(
            AgeGroupType category,
            BigDecimal dose,
            UOM unit,
            MedRoa rout,
            MedFrequency frequency
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (category != null) {
                predicates.add(cb.equal(root.get("category"), category));
            }
            if (dose != null) {
                predicates.add(cb.equal(root.get("dose"), dose));
            }
            if (unit != null) {
                predicates.add(cb.equal(root.get("unit"), unit));
            }
            if (rout != null) {
                predicates.add(cb.equal(root.get("rout"), rout));
            }
            if (frequency != null) {
                predicates.add(cb.equal(root.get("frequency"), frequency));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
