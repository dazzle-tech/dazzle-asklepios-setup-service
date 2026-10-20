package com.dazzle.asklepios.web.rest.vm.analyzer;

import com.dazzle.asklepios.domain.Analyzer;

import java.io.Serializable;

public record AnalyzerResponseVM(
        Long id,
        String code,
        String type,
        Boolean isActive
) implements Serializable {

    public static AnalyzerResponseVM ofEntity(Analyzer analyzer) {
        return new AnalyzerResponseVM(
                analyzer.getId(),
                analyzer.getCode(),
                analyzer.getType(),
                analyzer.getIsActive()
        );
    }
}
