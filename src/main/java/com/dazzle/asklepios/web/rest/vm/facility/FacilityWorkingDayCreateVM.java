package com.dazzle.asklepios.web.rest.vm.facility;

import com.dazzle.asklepios.domain.FacilityWorkingDay;
import com.dazzle.asklepios.domain.enumeration.DayOfWeek;
import jakarta.validation.constraints.NotNull;

public record FacilityWorkingDayCreateVM(
        @NotNull DayOfWeek dayOfWeek,
        @NotNull Boolean isWorking
) {
    public static FacilityWorkingDayCreateVM ofEntity(FacilityWorkingDay entity) {
        if (entity == null) return null;
        return new FacilityWorkingDayCreateVM(
                entity.getDayOfWeek(),
                entity.getIsWorking()
        );
    }
}