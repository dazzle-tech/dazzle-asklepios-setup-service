package com.dazzle.asklepios.web.rest.vm.catalog;

import com.dazzle.asklepios.domain.Catalog;
import com.dazzle.asklepios.domain.enumeration.TestType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CatalogResponseVM {
    private Long id;
    private String name;
    private String description;
    private TestType type;
    private Long departmentId;
    private String departmentName;

    public static CatalogResponseVM ofEntity(Catalog c) {
        return CatalogResponseVM.builder()
                .id(c.getId())
                .name(c.getName())
                .description(c.getDescription())
                .type(c.getType())
                .departmentId(c.getDepartment().getId())
                .departmentName(c.getDepartment().getName())
                .build();
    }
}
