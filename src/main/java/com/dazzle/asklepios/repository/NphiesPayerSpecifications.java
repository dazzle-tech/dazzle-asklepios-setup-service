package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.NphiesPayer;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class NphiesPayerSpecifications {

    private NphiesPayerSpecifications() {}

    /** Every argument is optional; the ones given are combined with AND (text = contains, case ignored). */
    public static Specification<NphiesPayer> search(String nphiesId, String nameEn, String nameAr, Boolean isActive) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (nphiesId != null && !nphiesId.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("nphiesId")), "%" + nphiesId.trim().toLowerCase(Locale.ROOT) + "%"));
            }
            if (nameEn != null && !nameEn.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("nameEn")), "%" + nameEn.trim().toLowerCase(Locale.ROOT) + "%"));
            }
            if (nameAr != null && !nameAr.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("nameAr")), "%" + nameAr.trim().toLowerCase(Locale.ROOT) + "%"));
            }
            if (isActive != null) {
                predicates.add(cb.equal(root.get("isActive"), isActive));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
