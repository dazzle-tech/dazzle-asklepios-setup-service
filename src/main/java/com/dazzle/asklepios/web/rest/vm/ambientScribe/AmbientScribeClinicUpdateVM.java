package com.dazzle.asklepios.web.rest.vm.ambientScribe;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record AmbientScribeClinicUpdateVM(
    @NotNull Long departmentId,
    @NotNull Boolean enabled,
    @NotNull List<Long> practitionerIds
) {
}
