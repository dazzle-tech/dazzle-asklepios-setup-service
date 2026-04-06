package com.dazzle.asklepios.web.rest.vm.departmentServices;

import com.dazzle.asklepios.domain.enumeration.patient.EncounterReason;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.util.List;

public record DepartmentServicesVM(
        @NotNull List<EncounterReason> services
) implements Serializable {
}