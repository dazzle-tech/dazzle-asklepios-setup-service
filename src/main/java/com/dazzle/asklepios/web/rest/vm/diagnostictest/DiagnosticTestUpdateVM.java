package com.dazzle.asklepios.web.rest.vm.diagnostictest;

import com.dazzle.asklepios.domain.enumeration.Currency;
import com.dazzle.asklepios.domain.enumeration.TestType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

public record DiagnosticTestUpdateVM(
        @NotNull(message = "id cannot be null")
        Long id,
        @NotBlank(message = "Type cannot be null")
        TestType type,
        @NotBlank(message = "Name cannot be null")
        String name,
        @NotBlank(message = "Code cannot be null")
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
        Boolean appointable

) implements Serializable {}
