package com.dazzle.asklepios.web.rest.vm.diagnostictest;


import com.dazzle.asklepios.domain.enumeration.Currency;
import com.dazzle.asklepios.domain.enumeration.TestType;
import jakarta.validation.constraints.NotEmpty;


import java.io.Serializable;
import java.math.BigDecimal;

public record DiagnosticTestCreateVM(
       @NotEmpty TestType type,
       @NotEmpty  String name,
       @NotEmpty String internalCode,
        Boolean ageSpecific,
        Boolean genderSpecific,
        String gender,
        Boolean specialPopulation,
        BigDecimal price,
        Currency currency,
        String specialNotes,
        Boolean isActive,
        Boolean isProfile,
        Boolean appointable

) implements Serializable {}
