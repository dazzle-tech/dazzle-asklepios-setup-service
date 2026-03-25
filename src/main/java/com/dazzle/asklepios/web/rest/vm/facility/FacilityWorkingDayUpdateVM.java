package com.dazzle.asklepios.web.rest.vm.facility;

import com.dazzle.asklepios.domain.FacilityWorkingDay;
import com.dazzle.asklepios.domain.enumeration.DayOfWeek;
import jakarta.validation.constraints.NotNull;

public record FacilityWorkingDayUpdateVM(
        @NotNull Long id,
        @NotNull Long facilityId,
        @NotNull DayOfWeek dayOfWeek,
        @NotNull Boolean isWorking
) {
    public static FacilityWorkingDayUpdateVM ofEntity(FacilityWorkingDay entity) {
        if (entity == null) return null;
        return new FacilityWorkingDayUpdateVM(
                entity.getId(),
                entity.getFacility()!=null? entity.getFacility().getId():null,
                entity.getDayOfWeek(),
                entity.getIsWorking()
        );
    }
}