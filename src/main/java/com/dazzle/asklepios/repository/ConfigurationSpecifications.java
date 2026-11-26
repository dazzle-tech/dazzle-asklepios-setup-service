package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.Configuration;
import com.dazzle.asklepios.domain.enumeration.ConfigurationKeys;
import com.dazzle.asklepios.domain.enumeration.ConfigurationReferenceType;
import com.dazzle.asklepios.domain.enumeration.ConfigurationScope;
import com.dazzle.asklepios.domain.enumeration.ConfigurationValueType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class ConfigurationSpecifications {

    private ConfigurationSpecifications() {}

    /** Every argument is optional; the ones given are combined with AND (text = contains, case ignored). */
    public static Specification<Configuration> search(
            ConfigurationScope scopeType,
            Long facilityId,
            ConfigurationKeys key,
            ConfigurationValueType valueType,
            String value,
            ConfigurationReferenceType referenceType,
            String description,
            Boolean isActive
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (scopeType != null) {
                predicates.add(cb.equal(root.get("scopeType"), scopeType));
            }
            if (facilityId != null) {
                predicates.add(cb.equal(root.get("facility").get("id"), facilityId));
            }
            if (key != null) {
                predicates.add(cb.equal(root.get("key"), key));
            }
            if (valueType != null) {
                predicates.add(cb.equal(root.get("valueType"), valueType));
            }
            if (value != null && !value.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("value")), "%" + value.trim().toLowerCase(Locale.ROOT) + "%"));
            }
            if (referenceType != null) {
                predicates.add(cb.equal(root.get("referenceType"), referenceType));
            }
            if (description != null && !description.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("description")), "%" + description.trim().toLowerCase(Locale.ROOT) + "%"));
            }
            if (isActive != null) {
                predicates.add(cb.equal(root.get("isActive"), isActive));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
