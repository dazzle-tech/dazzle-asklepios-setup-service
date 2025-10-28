package com.dazzle.asklepios.web.rest.vm.diagnostictest;


import com.dazzle.asklepios.domain.enumeration.Currency;
import com.dazzle.asklepios.domain.enumeration.TestType;
import java.io.Serializable;
import java.math.BigDecimal;

public record DiagnosticTestCreateVM(
        TestType testType,
        String testName,
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
        String createdBy
) implements Serializable {}
