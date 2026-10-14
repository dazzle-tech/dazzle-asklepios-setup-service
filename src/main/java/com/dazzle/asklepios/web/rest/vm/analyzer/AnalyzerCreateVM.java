package com.dazzle.asklepios.web.rest.vm.analyzer;

import com.dazzle.asklepios.domain.Analyzer;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotEmpty;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AnalyzerCreateVM(
        @NotEmpty String code,
        String type,
        Boolean isActive
) implements Serializable {

    public static AnalyzerCreateVM ofEntity(Analyzer analyzer) {
        return new AnalyzerCreateVM(
                analyzer.getCode(),
                analyzer.getType(),
                analyzer.getIsActive()
        );
    }
}
