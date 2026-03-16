package com.dazzle.asklepios.web.rest.vm.organizationDefinition;

import com.dazzle.asklepios.domain.OrganizationDefinition;
import com.dazzle.asklepios.domain.OrganizationWorkingDay;
import com.dazzle.asklepios.domain.enumeration.DayOfWeek;
import jakarta.validation.constraints.NotNull;

public record OrganizationWorkingDayCreateVM(
        @NotNull DayOfWeek dayOfWeek,
        @NotNull Boolean isWorking
) {
    public static OrganizationWorkingDayCreateVM ofEntity(OrganizationWorkingDay entity) {
        if (entity == null) return null;
        return new OrganizationWorkingDayCreateVM(
                entity.getDayOfWeek(),
                entity.getIsWorking()
        );
    }
}