package com.dazzle.asklepios.web.rest.vm.vaccineDoses;

import com.dazzle.asklepios.domain.VaccineDoses;
import com.dazzle.asklepios.domain.enumeration.AgeUnit;
import com.dazzle.asklepios.domain.enumeration.DoseNumber;
import java.io.Serializable;
import java.math.BigDecimal;

public record VaccineDosesResponseVM(
        Long id,
        DoseNumber doseNumber,
        BigDecimal fromAge,
        BigDecimal toAge,
        AgeUnit fromAgeUnit,
        AgeUnit toAgeUnit,
        Boolean isBooster,
        Boolean isActive
) implements Serializable {

    public static VaccineDosesResponseVM ofEntity(VaccineDoses dose) {
        return new VaccineDosesResponseVM(
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
