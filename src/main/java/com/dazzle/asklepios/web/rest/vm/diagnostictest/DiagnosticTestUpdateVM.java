package com.dazzle.asklepios.web.rest.vm.diagnostictest;

import com.dazzle.asklepios.domain.enumeration.AgeGroupType;
import com.dazzle.asklepios.domain.enumeration.Currency;
import com.dazzle.asklepios.domain.enumeration.TestType;
import jakarta.validation.constraints.NotEmpty;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

public record DiagnosticTestUpdateVM(
        @NotEmpty Long id,
        @NotEmpty TestType type,
        @NotEmpty String name,
        @NotEmpty String internalCode,
        Boolean ageSpecific,
        List<AgeGroupType> ageGroupList,           // ✅ جديد
        Boolean genderSpecific,
        String gender,
        Boolean specialPopulation,
        List<String> specialPopulationValues,      // ✅ جديد
        BigDecimal price,
        Currency currency,
        String specialNotes,
        Boolean isActive,
        Boolean isProfile,
        Boolean appointable,
        String lastModifiedBy

) implements Serializable {}
