package com.dazzle.asklepios.web.rest.vm.practitionerDepartment;

import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

/** View Model for creating a Practitioner–Department link. */
public record PractitionerDepartmentCreateVM(
        @NotNull Long practitionerId,
        @NotNull Long departmentId
) implements Serializable {}
