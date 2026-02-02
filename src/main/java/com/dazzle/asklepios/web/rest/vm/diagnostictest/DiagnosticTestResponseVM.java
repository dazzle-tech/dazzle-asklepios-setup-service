package com.dazzle.asklepios.web.rest.vm.diagnostictest;

import com.dazzle.asklepios.domain.DiagnosticTest;
import com.dazzle.asklepios.domain.enumeration.Currency;
import com.dazzle.asklepios.domain.enumeration.TestResultType;
import com.dazzle.asklepios.domain.enumeration.TestType;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record DiagnosticTestResponseVM(
        Long id,
        TestType type,
        String name,
        String internalCode,
        Boolean ageSpecific,
        List<String> ageGroupList,
        Boolean genderSpecific,
        String gender,
        Boolean specialPopulation,
        List<String> specialPopulationValues,
        BigDecimal price,
        Currency currency,
        String specialNotes,
        Boolean isActive,
        Boolean appointable,
        Instant createdDate,
        Instant lastModifiedDate,

        Long defaultProfileId,
        String defaultProfileResultUnit,
        TestResultType defaultProfileResultType,
        String listOfValueId
) implements Serializable {

    public static DiagnosticTestResponseVM ofEntity(DiagnosticTest test) {
        return new DiagnosticTestResponseVM(
                test.getId(),
                test.getType(),
                test.getName(),
                test.getInternalCode(),
                test.getAgeSpecific(),
                test.getAgeGroupList(),
                test.getGenderSpecific(),
                test.getGender(),
                test.getSpecialPopulation(),
                test.getSpecialPopulationValues(),
                test.getPrice(),
                test.getCurrency(),
                test.getSpecialNotes(),
                test.getIsActive(),
                test.getAppointable(),
                test.getCreatedDate(),
                test.getLastModifiedDate(),

                null, null, null, null
        );
    }

    public DiagnosticTestResponseVM withDefaultProfile(Long id, String unit, TestResultType type, String listOfValueId) {
        return new DiagnosticTestResponseVM(
                this.id, this.type, this.name, this.internalCode,
                this.ageSpecific, this.ageGroupList, this.genderSpecific, this.gender,
                this.specialPopulation, this.specialPopulationValues, this.price, this.currency,
                this.specialNotes, this.isActive, this.appointable,
                this.createdDate, this.lastModifiedDate,
                id, unit, type, listOfValueId
        );
    }
}

