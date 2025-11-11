package com.dazzle.asklepios.web.rest.vm.vaccineDosesInterval;

import com.dazzle.asklepios.domain.VaccineDosesInterval;
import com.dazzle.asklepios.domain.enumeration.AgeUnit;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.io.Serializable;
import java.math.BigDecimal;

public record VaccineDosesIntervalUpdateVM(
        @NotNull Long id,
        @NotNull Long fromDoseId,
        @NotNull @PositiveOrZero Long toDoseId,
        @NotNull BigDecimal intervalBetweenDoses,
        @NotNull AgeUnit unit,
        @NotNull Boolean isActive
) implements Serializable {

    public static VaccineDosesIntervalUpdateVM ofEntity(VaccineDosesInterval interval) {
        return new VaccineDosesIntervalUpdateVM(
                interval.getId(),
                interval.getFromDose() != null ? interval.getFromDose().getId() : null,
                interval.getToDose()   != null ? interval.getToDose().getId()   : null,
                interval.getIntervalBetweenDoses(),
                interval.getUnit(),
                interval.getIsActive()
        );
    }
}
