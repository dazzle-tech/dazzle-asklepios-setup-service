package com.dazzle.asklepios.web.rest.vm.payor;

import com.dazzle.asklepios.domain.Payor;
import com.dazzle.asklepios.domain.enumeration.biling.PayorCategory;

import java.time.Instant;
import java.time.LocalDate;

public record PayorResponseVM(
        Long id,
        String code,
        String name,
        PayorCategory category,

        String address,
        String phone,
        String email,
        String contractManagerContact,

        LocalDate startDate,
        LocalDate expiryDate,
        Boolean renewable,

        Boolean allowPartialCoverage,
        Boolean acceptCopay,
        Boolean acceptDeductibles,
        Boolean allowPackagePricing,
        Boolean allowDrgBilling,
        Boolean forcePreApproval,

        Boolean isActive,
        Instant createdDate,
        Instant lastModifiedDate
) {
    public static PayorResponseVM ofEntity(Payor p) {
        return new PayorResponseVM(
                p.getId(),
                p.getCode(),
                p.getName(),
                p.getCategory(),

                p.getAddress(),
                p.getPhone(),
                p.getEmail(),
                p.getContractManagerContact(),

                p.getStartDate(),
                p.getExpiryDate(),
                p.getRenewable(),

                p.getAllowPartialCoverage(),
                p.getAcceptCopay(),
                p.getAcceptDeductibles(),
                p.getAllowPackagePricing(),
                p.getAllowDrgBilling(),
                p.getForcePreApproval(),

                p.getIsActive(),
                p.getCreatedDate(),
                p.getLastModifiedDate()
        );
    }
}

