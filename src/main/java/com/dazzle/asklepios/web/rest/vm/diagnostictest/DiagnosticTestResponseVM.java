package com.dazzle.asklepios.web.rest.vm.diagnostictest;

import com.dazzle.asklepios.domain.DiagnosticTest;
import com.dazzle.asklepios.domain.enumeration.Currency;
import com.dazzle.asklepios.domain.enumeration.TestType;
import java.io.Serializable;
import java.math.BigDecimal;

public record DiagnosticTestResponseVM(
        Long id,
        TestType type,
        String name,
        String internalCode,
        Boolean ageSpecific,
        Boolean genderSpecific,
        String gender,
        Boolean specialPopulation,
        BigDecimal price,
        Currency currency,
        String specialNotes,
        Boolean isActive,
        Boolean appointable,
        Boolean isProfile
) implements Serializable {
    public static DiagnosticTestResponseVM ofEntity(DiagnosticTest test) {
        return new DiagnosticTestResponseVM(
                test.getId(),
                test.getType(),
                test.getName(),
                test.getInternalCode(),
                test.getAgeSpecific(),
                test.getGenderSpecific(),
                test.getGender(),
                test.getSpecialPopulation(),
                test.getPrice(),
                test.getCurrency(),
                test.getSpecialNotes(),
                test.getIsActive(),
                test.getAppointable(),
                test.getIsProfile()
        );
    }
}
