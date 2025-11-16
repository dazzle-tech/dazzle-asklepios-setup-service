package com.dazzle.asklepios.web.rest.vm.catalog;

import com.dazzle.asklepios.domain.Catalog;
import com.dazzle.asklepios.domain.enumeration.TestType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class CatalogCreateVM {
    @NotNull @Size(min = 2, max = 150)
    private String name;

    private String description;

    @NotNull
    private TestType type;

    @NotNull
    private Long departmentId;

}
