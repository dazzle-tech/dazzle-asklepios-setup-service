package com.dazzle.asklepios.web.rest.vm.vaccine;

import com.dazzle.asklepios.domain.Vaccine;
import com.dazzle.asklepios.domain.enumeration.VaccineType;
import com.dazzle.asklepios.domain.enumeration.RouteOfAdministration;
import com.dazzle.asklepios.domain.enumeration.DurationUnit;
import com.dazzle.asklepios.domain.enumeration.NumberOfDoses;

import java.io.Serializable;
import java.math.BigDecimal;

public record VaccineResponseVM(
        Long id,
        String name,
        VaccineType type,
        RouteOfAdministration roa,
        String atcCode,
        String siteOfAdministration,
        BigDecimal postOpeningDuration,
        DurationUnit durationUnit,
        NumberOfDoses numberOfDoses,
        String indications,
        String possibleReactions,
        String contraindicationsAndPrecautions,
        String storageAndHandling,
        Boolean isActive
) implements Serializable {

    public static VaccineResponseVM ofEntity(Vaccine vaccine) {
        return new VaccineResponseVM(
                vaccine.getId(),
                vaccine.getName(),
                vaccine.getType(),
                vaccine.getRoa(),
                vaccine.getAtcCode(),
                vaccine.getSiteOfAdministration(),
                vaccine.getPostOpeningDuration(),
                vaccine.getDurationUnit(),
                vaccine.getNumberOfDoses(),
                vaccine.getIndications(),
                vaccine.getPossibleReactions(),
                vaccine.getContraindicationsAndPrecautions(),
                vaccine.getStorageAndHandling(),
                vaccine.getIsActive()
        );
    }
}
