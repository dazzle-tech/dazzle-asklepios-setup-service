package com.dazzle.asklepios.web.rest.vm.coverage;

import com.dazzle.asklepios.domain.enumeration.biling.BillingItemTypes;

/**
 * One item a limit reading does not apply to. A null catalog item id means
 * the whole category is excluded.
 */
public record CoverageExcludedItemVM(
        BillingItemTypes billingItemType,
        Long catalogItemId
) {}
