package com.dazzle.asklepios.web.rest.vm.diagnostictest;

import com.dazzle.asklepios.domain.DiagnosticTest;
import com.dazzle.asklepios.domain.enumeration.Currency;
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
        Boolean isProfile,
        Boolean appointable,
        Instant createdDate,
        Instant lastModifiedDate
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
                test.getIsProfile(),
                test.getAppointable(),
                test.getCreatedDate(),
                test.getLastModifiedDate()
        );
    }
}