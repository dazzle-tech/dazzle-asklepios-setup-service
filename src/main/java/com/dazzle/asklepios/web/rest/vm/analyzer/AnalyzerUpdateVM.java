package com.dazzle.asklepios.web.rest.vm.analyzer;

import com.dazzle.asklepios.domain.Analyzer;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

public record AnalyzerUpdateVM(
        @NotNull Long id,
        @NotEmpty String code,
        String type,
        @NotNull Boolean isActive
) implements Serializable {

    public static AnalyzerUpdateVM ofEntity(Analyzer analyzer) {
        return new AnalyzerUpdateVM(
                analyzer.getId(),
                analyzer.getCode(),
                analyzer.getType(),
                analyzer.getIsActive()
        );
    }
}
