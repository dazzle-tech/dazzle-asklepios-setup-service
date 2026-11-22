package com.dazzle.asklepios.web.rest.vm.packageSetup;

import com.dazzle.asklepios.domain.enumeration.PriceListItemType;

import java.io.Serializable;
import java.math.BigDecimal;

/** An item that can be added to a package, read from its own setup table. */
public record PackageItemLookupVM(
        PriceListItemType itemType,
        Long sourceId,
        String code,
        String name,
        BigDecimal price
) implements Serializable {}
