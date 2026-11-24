package com.dazzle.asklepios.web.rest.vm.packageSetup;

import com.dazzle.asklepios.domain.Discount;
import com.dazzle.asklepios.domain.PackageSetup;
import com.dazzle.asklepios.domain.PackageSetupItem;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record PackageSetupResponseVM(
        Long id,
        String code,
        String name,
        String description,
        BigDecimal price,
        Boolean isActive,
        LocalDate validFrom,
        LocalDate validTo,
        Long discountId,
        String discountCode,
        String discountName,
        Boolean allDepartments,
        List<Long> departmentIds,
        Integer itemCount,
        Integer activeItemCount,
        List<PackageSetupItemVM> items
) implements Serializable {

    public static PackageSetupResponseVM ofEntity(
            PackageSetup entity,
            List<PackageSetupItem> items,
            List<Long> departmentIds,
            Discount discount
    ) {
        List<PackageSetupItemVM> lines = items.stream().map(PackageSetupItemVM::ofEntity).toList();
        return new PackageSetupResponseVM(
                entity.getId(),
                entity.getCode(),
                entity.getName(),
                entity.getDescription(),
                entity.getPrice(),
                entity.getIsActive(),
                entity.getValidFrom(),
                entity.getValidTo(),
                entity.getDiscountId(),
                discount == null ? null : discount.getCode(),
                discount == null ? null : discount.getName(),
                entity.getAllDepartments(),
                departmentIds,
                lines.size(),
                (int) lines.stream().filter(l -> !Boolean.FALSE.equals(l.isActive())).count(),
                lines
        );
    }
}
