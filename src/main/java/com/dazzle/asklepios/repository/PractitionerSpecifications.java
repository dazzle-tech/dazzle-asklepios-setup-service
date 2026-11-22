package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.Practitioner;
import com.dazzle.asklepios.domain.enumeration.JobRole;
import com.dazzle.asklepios.domain.enumeration.Specialty;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class PractitionerSpecifications {

    private PractitionerSpecifications() {}

    /**
     * Every argument is optional; the ones given are combined with AND.
     * Name matches the first or the last name; linkedToUser checks whether a user is linked.
     */
    public static Specification<Practitioner> search(
            Boolean isActive,
            Long facilityId,
            String name,
            Specialty specialty,
            JobRole jobRole,
            Boolean linkedToUser
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (isActive != null) {
                predicates.add(cb.equal(root.get("isActive"), isActive));
            }
            if (facilityId != null) {
                predicates.add(cb.equal(root.get("facility").get("id"), facilityId));
            }
            if (name != null && !name.isBlank()) {
                String like = "%" + name.trim().toLowerCase(Locale.ROOT) + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("firstName")), like),
                        cb.like(cb.lower(root.get("lastName")), like)
                ));
            }
            if (specialty != null) {
                predicates.add(cb.equal(root.get("specialty"), specialty));
            }
            if (jobRole != null) {
                predicates.add(cb.equal(root.get("jobRole"), jobRole));
            }
            if (linkedToUser != null) {
                predicates.add(linkedToUser ? cb.isNotNull(root.get("user")) : cb.isNull(root.get("user")));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
