package com.dazzle.asklepios.web.rest.vm.catalog;

import com.dazzle.asklepios.domain.CatalogDiagnosticTest;
import org.springframework.data.domain.Page;

import java.io.Serializable;
import java.util.List;


public record CatalogTestVM(
        List<Long> testIds,
        int page,
        int size,
        long totalElements,
        int totalPages
) implements Serializable {

    public static CatalogTestVM ofPage(Page<CatalogDiagnosticTest> page) {
        return new CatalogTestVM(
                page.stream()
                        .map(cdt -> cdt.getTest().getId())
                        .toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}



