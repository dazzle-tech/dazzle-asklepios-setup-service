package com.dazzle.asklepios.web.rest.vm.uom;

import com.dazzle.asklepios.domain.UomGroupsRelation;
import com.dazzle.asklepios.domain.enumeration.UOM;

import java.io.Serializable;
import java.math.BigDecimal;

public record UomGroupsRelationResponseVM (
    Long id,
    BigDecimal relation,
    Long groupId,
    Long fromUnitId,
    UOM fromUnit,
    Long toUnitId,
    UOM toUnit
) implements Serializable
 {

     public static UomGroupsRelationResponseVM of (UomGroupsRelation e){
        return new UomGroupsRelationResponseVM(
                e.getId(),
                e.getRelation(),
                e.getGroup().getId(),
                e.getFromUnit().getId(),
                e.getFromUnit().getUom(),
                e.getToUnit().getId(),
                e.getToUnit().getUom()
        );
    }

    }