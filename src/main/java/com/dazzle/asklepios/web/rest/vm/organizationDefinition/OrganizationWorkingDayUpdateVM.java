package com.dazzle.asklepios.web.rest.vm.organizationDefinition;

import com.dazzle.asklepios.domain.OrganizationWorkingDay;
import com.dazzle.asklepios.domain.enumeration.DayOfWeek;
import jakarta.validation.constraints.NotNull;

public record OrganizationWorkingDayUpdateVM(
        @NotNull Long id,
        @NotNull DayOfWeek dayOfWeek,
        @NotNull Boolean isWorking
) {
    public static OrganizationWorkingDayUpdateVM ofEntity(OrganizationWorkingDay entity) {
        if (entity == null) return null;
        return new OrganizationWorkingDayUpdateVM(
                entity.getId(),
                entity.getDayOfWeek(),
                entity.getIsWorking()
        );
    }
}