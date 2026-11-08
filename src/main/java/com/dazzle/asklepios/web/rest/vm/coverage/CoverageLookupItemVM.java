package com.dazzle.asklepios.web.rest.vm.coverage;

import com.dazzle.asklepios.domain.enumeration.EncounterType;

import java.time.LocalDate;

public record CoverageLookupItemVM(
        Long id,
        String code,
        String name,
        String extra,
        EncounterType encounterType,
        LocalDate startDate,
        LocalDate endDate,
        Boolean isActive,
        Long relatedId,
        String relatedName
) {
    public CoverageLookupItemVM(
            Long id,
            String code,
            String name,
            String extra,
            EncounterType encounterType,
            LocalDate startDate,
            LocalDate endDate,
            Boolean isActive
    ) {
        this(id, code, name, extra, encounterType, startDate, endDate, isActive, null, null);
    }

    public static CoverageLookupItemVM of(Long id, String code, String name) {
        return new CoverageLookupItemVM(id, code, name, null, null, null, null, Boolean.TRUE);
    }
}
