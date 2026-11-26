package com.dazzle.asklepios.web.rest.vm.coverage;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CoverageDiagnosisRefVM(
        Long diagnosisId,
        String diagnosisCode,
        String diagnosisName
) {}
