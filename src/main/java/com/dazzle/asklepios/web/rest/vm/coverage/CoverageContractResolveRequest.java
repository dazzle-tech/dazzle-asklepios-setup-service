package com.dazzle.asklepios.web.rest.vm.coverage;

import java.time.LocalDate;
import java.util.List;

public record CoverageContractResolveRequest(
        Long insurancePayerId,
        String payerNphiesId,
        Long tpaId,
        String tpaName,
        String policyNumber,
        String className,
        String encounterType,
        LocalDate asOfDate,
        Long facilityId,
        Long departmentId,
        List<Long> diagnosisIds,
        String billingItemType,
        Long catalogItemId
) {
    public CoverageContractResolveRequest(
            Long insurancePayerId,
            String payerNphiesId,
            Long tpaId,
            String tpaName,
            String policyNumber,
            String className,
            String encounterType,
            LocalDate asOfDate
    ) {
        this(
                insurancePayerId,
                payerNphiesId,
                tpaId,
                tpaName,
                policyNumber,
                className,
                encounterType,
                asOfDate,
                null,
                null,
                null,
                null,
                null
        );
    }
}
