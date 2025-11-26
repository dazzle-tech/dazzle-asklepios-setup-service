package com.dazzle.asklepios.web.rest.vm.catalog;

import com.dazzle.asklepios.domain.CatalogDiagnosticTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.io.Serializable;
import java.util.List;

public record CatalogTestVM(
        List<TestItemVM> tests
) implements Serializable {

//public static Page<CatalogTestVM> ofPage(Page<CatalogDiagnosticTest> page) {
//    List<CatalogTestVM> vmList = page.getContent().stream()
//            .map(cdt -> new CatalogTestVM(
//                    List.of(new CatalogTestVM.TestItemVM(
//                            cdt.getTest().getId(),
//                            cdt.getTest().getInternalCode(),
//                            cdt.getTest().getName()
//                    ))
//            ))
//            .toList();
//
//    return new PageImpl<>(vmList, page.getPageable(), page.getTotalElements());
//}

    public static CatalogTestVM ofPage(Page<CatalogDiagnosticTest> page) {
        List<CatalogTestVM.TestItemVM> allTests = page.stream()
                .map(cdt -> new CatalogTestVM.TestItemVM(
                        cdt.getTest().getId(),
                        cdt.getTest().getInternalCode(),
                        cdt.getTest().getName()
                ))
                .toList();

        return new CatalogTestVM(allTests);
    }

    public record TestItemVM(
            Long id,
            String code,
            String name
    ) implements Serializable {}
}