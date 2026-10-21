package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.CoverageContract;
import com.dazzle.asklepios.domain.NphiesPayer;
import com.dazzle.asklepios.domain.PriceListSetup;
import com.dazzle.asklepios.domain.enumeration.GuarantorType;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
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
            if (search != null && !search.isBlank() && query != null) {
                String like = "%" + search.trim().toLowerCase(Locale.ROOT) + "%";
                Subquery<Long> payerIds = query.subquery(Long.class);
                Root<NphiesPayer> payer = payerIds.from(NphiesPayer.class);
                payerIds.select(payer.get("id"));
                payerIds.where(cb.or(
                        cb.like(cb.lower(payer.get("nameEn")), like),
                        cb.like(cb.lower(payer.get("nameAr")), like),
                        cb.like(cb.lower(payer.get("nphiesId")), like)
                ));

                Subquery<Long> priceListIds = query.subquery(Long.class);
                Root<PriceListSetup> priceList = priceListIds.from(PriceListSetup.class);
                priceListIds.select(priceList.get("id"));
                priceListIds.where(cb.like(cb.lower(priceList.get("name")), like));

                predicates.add(cb.or(
                        root.get("insurancePayerId").in(payerIds),
                        root.get("priceListSetupId").in(priceListIds)
                ));
            }
            return cb.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
    }
}
