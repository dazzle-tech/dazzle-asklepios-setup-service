package com.dazzle.asklepios.web.rest.vm.packageSetup;

import com.dazzle.asklepios.domain.PackageSetup;
import com.dazzle.asklepios.domain.PackageSetupItem;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

public record PackageSetupResponseVM(
        Long id,
        String code,
        String name,
        String description,
        BigDecimal price,
        Boolean isActive,
        Integer itemCount,
        List<PackageSetupItemVM> items
) implements Serializable {

    public static PackageSetupResponseVM ofEntity(PackageSetup entity, List<PackageSetupItem> items) {
        List<PackageSetupItemVM> lines = items.stream().map(PackageSetupItemVM::ofEntity).toList();
        return new PackageSetupResponseVM(
                entity.getId(),
                entity.getCode(),
                entity.getName(),
                entity.getDescription(),
                entity.getPrice(),
                entity.getIsActive(),
                lines.size(),
                lines
        );
    }
}
