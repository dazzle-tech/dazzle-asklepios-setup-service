package com.dazzle.asklepios.web.rest.vm.ambientScribe;

import java.util.List;

public record AmbientScribeClinicVM(
    Long id,
    Long departmentId,
    String departmentName,
    Boolean enabled,
    List<AmbientScribePractitionerVM> practitioners
) {
}
