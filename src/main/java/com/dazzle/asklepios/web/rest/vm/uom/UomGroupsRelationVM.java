package com.dazzle.asklepios.web.rest.vm.uom;

import java.math.BigDecimal;

public record UomGroupsRelationVM(
        Long id,
        BigDecimal relation,
        Long fromUnitId,
        Long toUnitId
) {}
