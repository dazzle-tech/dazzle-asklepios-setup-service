package com.dazzle.asklepios.web.rest.vm.catalog;

import com.dazzle.asklepios.domain.CatalogDiagnosticTest;
import org.springframework.data.domain.Page;

import java.io.Serializable;
import java.util.List;

public record CatalogTestVM(
        List<TestItemVM> tests,
        int page,
        int size,
        long totalElements,
        int totalPages
) implements Serializable {

    public static CatalogTestVM ofPage(Page<CatalogDiagnosticTest> page) {
        return new CatalogTestVM(
                page.stream()
                        .map(cdt -> new TestItemVM(
                                cdt.getTest().getId(),
                                cdt.getTest().getInternalCode(),
                                cdt.getTest().getName()
                        ))
                        .toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    public record TestItemVM(
            Long id,
            String code,
            String name
    ) implements Serializable {}
}