package com.dazzle.asklepios.web.rest.vm.diagnostictest;

import com.dazzle.asklepios.domain.enumeration.Currency;
import com.dazzle.asklepios.domain.enumeration.TestResultType;
import com.dazzle.asklepios.domain.enumeration.TestType;
import com.dazzle.asklepios.service.validation.ValidDiagnosticTest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
@ValidDiagnosticTest
public record DiagnosticTestCreateVM(
        @NotNull(message = "Type cannot be null")
        TestType type,
        @NotBlank(message = "Name cannot be null")
        String name,
        @NotBlank(message = "Code cannot be null")
        String internalCode,
        String hl7IntegrationCode,
        String shortName,
        Boolean ageSpecific,
        List<String> ageGroupList,
        Boolean genderSpecific,
        String gender,
        Boolean specialPopulation,
        List<String> specialPopulationValues,
        @NotNull(message = "Price cannot be null")
        BigDecimal price,
        Currency currency,
        String specialNotes,
        Boolean isActive,

        Boolean appointable,
        Integer parallelCapacityValue,
        Integer defaultDurationMinutes,
        Integer defaultBufferBeforeMinutes,
        Integer defaultBufferAfterMinutes,
        TestResultType defaultProfileResultType,
        String defaultProfileResultUnit,
        String listOfValueId,
        String modality,
        Long billingRuleId
) implements Serializable {}