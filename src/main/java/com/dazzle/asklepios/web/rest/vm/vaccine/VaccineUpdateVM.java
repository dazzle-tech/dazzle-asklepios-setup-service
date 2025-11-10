package com.dazzle.asklepios.web.rest.vm.vaccine;

import com.dazzle.asklepios.domain.Vaccine;
import com.dazzle.asklepios.domain.enumeration.VaccineType;
import com.dazzle.asklepios.domain.enumeration.RouteOfAdministration;
import com.dazzle.asklepios.domain.enumeration.DurationUnit;
import com.dazzle.asklepios.domain.enumeration.NumberOfDoses;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * View Model for updating a Vaccine via REST.
 */
public record VaccineUpdateVM(
        @NotNull Long id,
        @NotEmpty String name,
        @NotNull VaccineType type,
        @NotNull RouteOfAdministration roa,
        String atcCode,
        String siteOfAdministration,
        BigDecimal postOpeningDuration,
        DurationUnit durationUnit,
        @NotNull NumberOfDoses numberOfDoses,
        String indications,
        String possibleReactions,
        String contraindicationsAndPrecautions,
        String storageAndHandling,
        @NotNull Boolean isActive
) implements Serializable {

    public static VaccineUpdateVM ofEntity(Vaccine vaccine) {
        return new VaccineUpdateVM(
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
