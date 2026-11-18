package com.dazzle.asklepios.web.rest.vm.vitalsignrange;

import com.dazzle.asklepios.domain.VitalSignRange;

import java.math.BigDecimal;

public record VitalSignRangeResponseVM(
        Long id,
        String vitalSignKey,
        String unit,
        Integer ageFromMonths,
        Integer ageToMonths,
        BigDecimal normalMin,
        BigDecimal normalMax
) {
    public static VitalSignRangeResponseVM fromEntity(VitalSignRange entity) {
        return new VitalSignRangeResponseVM(
                entity.getId(),
                entity.getVitalSignKey(),
                entity.getUnit(),
                entity.getAgeFromMonths(),
                entity.getAgeToMonths(),
                entity.getNormalMin(),
                entity.getNormalMax());
    }
}
