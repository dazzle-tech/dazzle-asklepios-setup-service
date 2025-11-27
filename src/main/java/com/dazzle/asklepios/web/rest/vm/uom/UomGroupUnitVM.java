package com.dazzle.asklepios.web.rest.vm.uom;

import com.dazzle.asklepios.domain.enumeration.UOM;
import java.math.BigDecimal;

public record UomGroupUnitVM(
        Long id,
        UOM uom,
        BigDecimal uomOrder
) {}
