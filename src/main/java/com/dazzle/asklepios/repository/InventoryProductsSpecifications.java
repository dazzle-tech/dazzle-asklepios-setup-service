package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.InventoryProducts;
import com.dazzle.asklepios.domain.enumeration.InventoryType;
import com.dazzle.asklepios.domain.enumeration.ProductTypes;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class InventoryProductsSpecifications {

    private InventoryProductsSpecifications() {}

    /** Optional filters for the product list; the ones given are combined with AND. */
    public record Filter(
            Boolean isActive,
            String name,
            ProductTypes type,
            String code,
            String barcode,
            String erpIntegrationId,
            Long uomGroupId,
            Long baseUom,
            Long dispenseUom,
            InventoryType inventoryType,
            LocalDate warrantyStartDate,
            LocalDate warrantyEndDate
    ) {}

    /**
     * Text filters are case-insensitive contains, ids and enums are exact.
     * Warranty: start on or after {@code warrantyStartDate}, end on or before {@code warrantyEndDate}.
     */
    public static Specification<InventoryProducts> search(Filter f) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (f.isActive() != null) {
                predicates.add(cb.equal(root.get("isActive"), f.isActive()));
            }
            addLike(predicates, cb, root.get("name"), f.name());
            addLike(predicates, cb, root.get("code"), f.code());
            addLike(predicates, cb, root.get("barcode"), f.barcode());
            addLike(predicates, cb, root.get("erpIntegrationId"), f.erpIntegrationId());
            addEqual(predicates, cb, root.get("type"), f.type());
            addEqual(predicates, cb, root.get("uomGroupId"), f.uomGroupId());
            addEqual(predicates, cb, root.get("baseUom"), f.baseUom());
            addEqual(predicates, cb, root.get("dispenseUom"), f.dispenseUom());
            addEqual(predicates, cb, root.get("inventoryType"), f.inventoryType());
            if (f.warrantyStartDate() != null) {
                Instant from = f.warrantyStartDate().atStartOfDay().toInstant(ZoneOffset.UTC);
                predicates.add(cb.greaterThanOrEqualTo(root.get("warrantyStartDate"), from));
            }
            if (f.warrantyEndDate() != null) {
                Instant to = f.warrantyEndDate().plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);
                predicates.add(cb.lessThan(root.get("warrantyEndDate"), to));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private static void addLike(List<Predicate> predicates, CriteriaBuilder cb, Path<String> path, String value) {
        if (value != null && !value.isBlank()) {
            predicates.add(cb.like(cb.lower(path), "%" + value.trim().toLowerCase(Locale.ROOT) + "%"));
        }
    }

    private static void addEqual(List<Predicate> predicates, CriteriaBuilder cb, Path<?> path, Object value) {
        if (value != null) {
            predicates.add(cb.equal(path, value));
        }
    }
}
