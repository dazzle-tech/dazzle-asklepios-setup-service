package com.dazzle.asklepios.web.rest.vm.uom;

import java.util.List;

public record UomGroupVM(
        Long id,
        String name,
        String description,
        List<UomGroupUnitVM> units,
        List<UomGroupsRelationVM> relations
) {}
