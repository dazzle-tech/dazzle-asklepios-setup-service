package com.dazzle.asklepios.web.rest.vm.vaccineDoses;

import com.dazzle.asklepios.domain.VaccineDoses;
import com.dazzle.asklepios.domain.enumeration.AgeUnit;
import com.dazzle.asklepios.domain.enumeration.DoseNumber;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.io.Serializable;
import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record VaccineDosesUpdateVM(
        @NotNull Long id,
        @NotNull DoseNumber doseNumber,
        @NotNull @PositiveOrZero BigDecimal fromAge,
        @NotNull @PositiveOrZero BigDecimal toAge,
        @NotNull AgeUnit fromAgeUnit,
        @NotNull AgeUnit toAgeUnit,
        @NotNull Boolean isBooster,
        @NotNull Boolean isActive
) implements Serializable {

    public static VaccineDosesUpdateVM ofEntity(VaccineDoses dose) {
        return new VaccineDosesUpdateVM(
                dose.getId(),
                dose.getDoseNumber(),
                dose.getFromAge(),
                dose.getToAge(),
                dose.getFromAgeUnit(),
                dose.getToAgeUnit(),
                dose.getIsBooster(),
                dose.getIsActive()
        );
    }
}
