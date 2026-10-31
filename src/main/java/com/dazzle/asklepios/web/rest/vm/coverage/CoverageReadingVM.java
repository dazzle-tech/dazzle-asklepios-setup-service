package com.dazzle.asklepios.web.rest.vm.coverage;

import com.dazzle.asklepios.domain.enumeration.CoverageBasis;
import com.dazzle.asklepios.domain.enumeration.CoveragePeriodBasis;
import com.dazzle.asklepios.domain.enumeration.CoverageRuleTarget;
import com.dazzle.asklepios.domain.enumeration.biling.BillingItemTypes;
import com.dazzle.asklepios.domain.enumeration.biling.InsuranceCoverageType;

import java.math.BigDecimal;
import java.util.List;

public record CoverageReadingVM(
        Long termId,
        Long itemId,
        CoverageRuleTarget categoryScope,
        BillingItemTypes billingItemType,
        Long serviceId,
        InsuranceCoverageType valueType,
        BigDecimal limitValue,
        CoveragePeriodBasis periodBasis,
        CoverageBasis coverageBasis,
        List<CoverageExcludedItemVM> excludedItems
) {
    public CoverageReadingVM {
        excludedItems = excludedItems == null ? List.of() : List.copyOf(excludedItems);
    }

    public CoverageReadingVM(
            Long termId,
            Long itemId,
            CoverageRuleTarget categoryScope,
            BillingItemTypes billingItemType,
            Long serviceId,
            InsuranceCoverageType valueType,
            BigDecimal limitValue,
            CoveragePeriodBasis periodBasis,
            CoverageBasis coverageBasis
    ) {
        this(
                termId,
                itemId,
                categoryScope,
                billingItemType,
                serviceId,
                valueType,
                limitValue,
                periodBasis,
                coverageBasis,
                List.of()
        );
    }

    public CoverageReadingVM(
            Long termId,
            Long itemId,
            CoverageRuleTarget categoryScope,
            BillingItemTypes billingItemType,
            Long serviceId,
            InsuranceCoverageType valueType,
            BigDecimal limitValue
    ) {
        this(
                termId,
                itemId,
                categoryScope,
                billingItemType,
                serviceId,
                valueType,
                limitValue,
                null,
                null,
                List.of()
        );
    }
}
