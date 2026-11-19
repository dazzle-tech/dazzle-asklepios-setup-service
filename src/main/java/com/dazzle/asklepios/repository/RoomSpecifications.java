package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.Room;
import com.dazzle.asklepios.domain.enumeration.Gender;
import com.dazzle.asklepios.domain.enumeration.RoomType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class RoomSpecifications {

    private RoomSpecifications() {}

    /**
     * Every argument is optional; the ones given are combined with AND.
     * Gender: {@code notGenderSpecific=true} keeps rooms that are not gender specific;
     * otherwise a {@code gender} keeps gender-specific rooms of that gender.
     */
    public static Specification<Room> search(
            Boolean isActive,
            String name,
            Long facilityId,
            String floor,
            Long departmentId,
            RoomType type,
            Gender gender,
            boolean notGenderSpecific,
            Integer parallelCapacityValue
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (isActive != null) {
                predicates.add(cb.equal(root.get("isActive"), isActive));
            }
            if (hasText(name)) {
                predicates.add(cb.like(cb.lower(root.get("name")), toLike(name)));
            }
            if (facilityId != null) {
                predicates.add(cb.equal(root.get("facility").get("id"), facilityId));
            }
            if (hasText(floor)) {
                predicates.add(cb.like(cb.lower(root.get("floor")), toLike(floor)));
            }
            if (departmentId != null) {
                predicates.add(cb.equal(root.get("department").get("id"), departmentId));
            }
            if (type != null) {
                predicates.add(cb.equal(root.get("type"), type));
            }
            if (notGenderSpecific) {
                predicates.add(cb.isFalse(root.get("isSpecificGender")));
            } else if (gender != null) {
                predicates.add(cb.isTrue(root.get("isSpecificGender")));
                predicates.add(cb.equal(root.get("gender"), gender));
            }
            if (parallelCapacityValue != null) {
                predicates.add(cb.equal(root.get("parallelCapacityValue"), parallelCapacityValue));
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
