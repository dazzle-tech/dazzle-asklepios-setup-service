package com.dazzle.asklepios.web.rest.vm.analyzer;

import com.dazzle.asklepios.domain.Analyzer;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AnalyzerCreateVM(
        @NotEmpty String code,
        @NotEmpty String type,
        @NotNull Long facilityId,
        Boolean isActive
) implements Serializable {

    public static AnalyzerCreateVM ofEntity(Analyzer analyzer) {
        return new AnalyzerCreateVM(
                analyzer.getCode(),
                analyzer.getType(),
                analyzer.getFacility() != null ? analyzer.getFacility().getId() : null,
                analyzer.getIsActive()
        );
    }
}
