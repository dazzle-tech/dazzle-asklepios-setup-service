package com.dazzle.asklepios.web.rest.vm.catalog;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/** payload to add tests to a catalog */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CatalogAddTestsVM {

    @NotEmpty
    private List<Long> testIds;
}
